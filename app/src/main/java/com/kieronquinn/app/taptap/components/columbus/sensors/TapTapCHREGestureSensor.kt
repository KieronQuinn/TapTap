package com.kieronquinn.app.taptap.components.columbus.sensors

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.google.pixel.vendor.PixelAtoms
import android.hardware.location.NanoAppMessage
import android.os.Handler
import android.os.PowerManager
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.android.columbus.proto.nano.ColumbusGesture
import com.google.android.columbus.sensors.CHREGestureSensor
import com.google.android.columbus.sensors.configuration.GestureConfiguration
import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository.ShizukuServiceResponse
import com.kieronquinn.app.taptap.utils.contexthub.ContextHubClientCallbackLocalToRemoteWrapper
import com.kieronquinn.app.taptap.utils.extensions.registerReceiverCompat
import com.kieronquinn.app.taptap.utils.extensions.runOnDestroy
import com.kieronquinn.app.taptap.utils.extensions.unregisterReceiverIfRegistered
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.app.taptap.utils.flow.FlowQueue
import com.kieronquinn.app.taptap.utils.logging.UiEventLogger
import com.kieronquinn.app.taptap.utils.statusbar.StatusBarStateController
import com.kieronquinn.app.taptap.utils.wakefulness.WakefulnessLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TapTapCHREGestureSensor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val serviceEventEmitter: ServiceEventEmitter,
    uiEventLogger: UiEventLogger,
    gestureConfiguration: GestureConfiguration,
    statusBarStateController: StatusBarStateController,
    wakefulnessLifecycle: WakefulnessLifecycle,
    private val bgHandler: Handler,
    private val shizuku: TapTapShizukuServiceRepository,
    private val isTripleTapEnabled: Boolean
) : CHREGestureSensor(
    context,
    uiEventLogger,
    gestureConfiguration,
    statusBarStateController,
    wakefulnessLifecycle,
    bgHandler
) {

    companion object {
        private const val TAG = "TapTapCHRE"
        private const val CLASS_REQUEST_SERVICE =
            "com.google.android.systemui.columbus.ColumbusTargetRequestService"
        private const val CLASS_REQUEST_SERVICE_LEGACY =
            "com.google.android.systemui.columbus.legacy.ColumbusTargetRequestService"
    }

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /**
     *  In the December 2021 Feature Drop, Columbus was modified slightly, tweaking when the
     *  single tap events are sent. There are two methods to handle this (old and new) in this
     *  class, and the check for the version is to look for the "Target" service, as is done
     *  in the extension
     */
    private val isLegacyColumbus = context.isLegacyColumbus()

    private val isScreenOn
        get() = powerManager.isInteractive

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateScreenState()
        }
    }

    override var screenOn
        get() = isScreenOn
        set(_) {
            //no-op, getter only
        }

    private suspend fun getRemoteContextHubClient(): IRemoteContextHubClient? {
        val result = shizuku.runWithService {
            try {
                it.remoteContextHubClient
            } catch (e: RemoteException) {
                null
            }
        }
        return when (result) {
            is ShizukuServiceResponse.Success -> {
                serviceEventEmitter.postServiceEvent(ServiceEventEmitter.ServiceEvent.Started)
                result.result
            }
            is ShizukuServiceResponse.Failed -> {
                serviceEventEmitter.postServiceEvent(ServiceEventEmitter.ServiceEvent.Failed(result.reason))
                null
            }
        }
    }

    override fun initializeContextHubClientIfNull() {
        //no-op
    }

    /*
        Tap, Tap has a problem unique to it vs Columbus - there's an extra layer of IPC to go through

        This makes message sending slow, and a race condition can easily happen when sending the
        initial messages, which happen in quick succession. The workaround for this is to buffer
        them (with a 250ms delay), and send them in batch.
     */

    data class NanoMessage(val messageType: Int, val bytes: ByteArray, val onFail: (() -> Unit)?, val onSuccess: (() -> Unit)?)
    private val messageBuffer = FlowQueue<NanoMessage>()

    override fun sendMessageToNanoApp(
        messageType: Int,
        bytes: ByteArray,
        onFail: (() -> Unit)?,
        onSuccess: (() -> Unit)?
    ) {
        lifecycleOwner.lifecycle.whenCreated {
            messageBuffer.add(NanoMessage(messageType, bytes, onFail, onSuccess))
        }
    }

    private suspend fun processNanoMessages(queue: ArrayDeque<NanoMessage>){
        val client = getContextHubClient() ?: run {
            queue.forEach { it.onFail?.invoke() }
            queue.clear()
            return
        }
        val callback =
            ContextHubClientCallbackLocalToRemoteWrapper(contextHubClientCallback)
        withContext(Dispatchers.IO){
            while(true){
                val item = queue.removeFirstOrNull() ?: break
                val nanoAppMessage =
                    NanoAppMessage.createMessageToNanoApp(NANO_APP_ID, item.messageType, item.bytes)
                val result = client.sendMessageToNanoApp(callback, nanoAppMessage)
                if (result != 0) {
                    Log.e(TAG, "Unable to send message $item.messageType to nanoapp, error code $result")
                    item.onFail?.invoke()
                } else {
                    item.onSuccess?.invoke()
                }
            }
        }
    }

    private val contextHubMutex = Mutex()
    private suspend fun getContextHubClient() = contextHubMutex.withLock {
        return@withLock getRemoteContextHubClient()
    }

    override fun handleNanoappEvents(nanoappEvents: ColumbusGesture.NanoAppEvents) {
        //Only required if triple tap is enabled and using legacy columbus
        if (!isTripleTapEnabled || !isLegacyColumbus) return
        val singleTaps = nanoappEvents.batchedEventsList
            .filter { it.type == PixelAtoms.DoubleTapNanoappEventReported.Type.SINGLE_TAP.number }
            .map { it.timestamp }
        if (singleTaps.isEmpty()) return
        handleTapEvents(singleTaps)
    }

    private val tapEvents = ArrayDeque<Long>()

    @Synchronized
    fun handleTapEvents(
        newTimestamps: List<Long>,
        handleDoubleTap: Boolean = false,
        handleTripleTap: Boolean = true
    ) {
        val timeout = TapTapTapRT.mMaxTimeGapTripleNs
        val uptime = SystemClock.elapsedRealtimeNanos()
        tapEvents.addAll(newTimestamps)
        tapEvents.removeIf { uptime - it > timeout }
        if (tapEvents.size >= 2) {
            if (handleDoubleTap) {
                reportGestureDetected(1, false)
                tapEvents.clear()
                return
            } else if (handleTripleTap) {
                //Delay is in ms, and has a 100ms buffer time taken off
                val delay = ((timeout - (uptime - tapEvents.first())) / 1000000L) - 100
                delayDoubleTapCheck(delay)
            }
        }
        if (tapEvents.size >= 3) {
            reportGestureDetected(3, false)
            clearDoubleTapCheck()
            tapEvents.clear()
            return
        }
        reportGestureDetected(2, true)
    }

    private val delayDoubleTapRunnable = Runnable {
        handleTapEvents(emptyList(), handleDoubleTap = true, handleTripleTap = false)
    }

    private fun delayDoubleTapCheck(delayBy: Long) {
        clearDoubleTapCheck()
        bgHandler.postDelayed(delayDoubleTapRunnable, delayBy)
    }

    private fun clearDoubleTapCheck() {
        bgHandler.removeCallbacks(delayDoubleTapRunnable)
    }

    private fun reportGestureDetected(flags: Int, isHapticConsumed: Boolean) {
        reportGestureDetected(flags, DetectionProperties(isHapticConsumed))
    }

    override fun handleGestureDetection(gestureDetected: ColumbusGesture.GestureDetected) {
        Log.d("Columbus/GestureSensor", "handleGestureDetection, legacy: $isLegacyColumbus")
        if (!isTripleTapEnabled){
            //Handle double taps with the standard flow if triple tap is disabled
            super.handleGestureDetection(gestureDetected)
        }else if(!isLegacyColumbus) {
            //Non-legacy columbus for triple tap
            handleTapEvents(listOf(SystemClock.elapsedRealtimeNanos()))
        }
    }

    override fun startListening(heuristicMode: Boolean) {
        context.registerReceiverCompat(screenStateReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        context.registerReceiverCompat(screenStateReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        super.startListening(heuristicMode)
    }

    override fun stopListening() {
        super.stopListening()
        context.unregisterReceiverIfRegistered(screenStateReceiver)
    }

    private fun Context.isLegacyColumbus(): Boolean {
        return arrayOf(CLASS_REQUEST_SERVICE, CLASS_REQUEST_SERVICE_LEGACY).all {
            val targetServiceIntent = Intent().apply {
                component = ComponentName("com.android.systemui", it)
                `package` = "com.android.systemui"
            }
            packageManager.queryIntentServices(targetServiceIntent, 0).isEmpty()
        }
    }

    init {
        lifecycleOwner.lifecycle.runOnDestroy {
            stopListening()
        }
        lifecycleOwner.lifecycle.whenCreated {
            messageBuffer.asFlow().debounce(250L).collect {
                processNanoMessages(messageBuffer.asQueue())
            }
        }
    }

}