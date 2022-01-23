package com.google.android.columbus.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.location.ContextHubClient
import android.hardware.location.ContextHubClientCallback
import android.hardware.location.ContextHubManager
import android.hardware.location.NanoAppMessage
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.android.internal.util.RingBuffer
import com.google.android.columbus.proto.nano.ColumbusGesture
import com.google.android.columbus.sensors.configuration.GestureConfiguration
import com.google.protobuf.InvalidProtocolBufferException
import com.kieronquinn.app.taptap.utils.logging.UiEventLogger
import com.kieronquinn.app.taptap.utils.statusbar.StatusBarStateController
import com.kieronquinn.app.taptap.utils.wakefulness.WakefulnessLifecycle

open class CHREGestureSensor(
    private val context: Context,
    private val uiEventLogger: UiEventLogger,
    private val gestureConfiguration: GestureConfiguration,
    private val statusBarStateController: StatusBarStateController,
    private val wakefulnessLifecycle: WakefulnessLifecycle,
    private val bgHandler: Handler
): GestureSensor() {

    companion object {
        private const val TAG = "Columbus/GestureSensor"
        const val NANO_APP_ID = 0x476F6F676C001019L
    }

    inner class FeatureVector(gestureDetected: ColumbusGesture.GestureDetected) {

        private val timestamp = SystemClock.elapsedRealtime()
        private val featureVectors = gestureDetected.featureVectorList
        private val gesture = gestureDetected.gestureType

    }

    inner class FeatureVectorDumper {

        private val featureVectors = RingBuffer(FeatureVector::class.java, 10)
        private var lastSingleTapFeatureVector: FeatureVector? = null
        private var secondToLastSingleTapFeatureVector: FeatureVector? = null

        fun onGestureDetected(gestureDetected: ColumbusGesture.GestureDetected){
            when(gestureDetected.gestureType){
                1 -> {
                    val secondToLast = secondToLastSingleTapFeatureVector
                    val last = lastSingleTapFeatureVector
                    secondToLastSingleTapFeatureVector = null
                    lastSingleTapFeatureVector = null
                    if(secondToLast != null && last != null){
                        featureVectors.append(secondToLast)
                        featureVectors.append(last)
                        return
                    }

                    Log.w(TAG, "Received double tap without single taps, event will not appear in sysdump")
                }
                2 -> {
                    secondToLastSingleTapFeatureVector = lastSingleTapFeatureVector
                    lastSingleTapFeatureVector = FeatureVector(gestureDetected)
                }
            }
        }

    }

    protected val contextHubClientCallback = object: ContextHubClientCallback() {

        override fun onHubReset(client: ContextHubClient) {
            Log.d(TAG, "HubReset: ${client.attachedHub.id}")
        }

        override fun onMessageFromNanoApp(client: ContextHubClient, message: NanoAppMessage) {
            if(message.nanoAppId != NANO_APP_ID) return
            try {
                when(message.messageType){
                    300 -> {
                        val gestureDetected = ColumbusGesture.GestureDetected.parseFrom(message.messageBody)
                        handleGestureDetection(gestureDetected)
                    }
                    500 -> {
                        val nanoappEvents = ColumbusGesture.NanoAppEvents.parseFrom(message.messageBody)
                        handleNanoappEvents(nanoappEvents)
                        return
                    }
                    else -> {
                        Log.e(TAG, "Unknown message type ${message.messageType}")
                        return
                    }
                }
            }catch (e: InvalidProtocolBufferException){

            }
        }

        override fun onNanoAppAborted(client: ContextHubClient, nanoAppId: Long, abortCode: Int) {
            if(nanoAppId == NANO_APP_ID){
                Log.e(TAG, "Nanoapp aborted, code $abortCode")
            }
        }

        override fun onNanoAppLoaded(client: ContextHubClient, nanoAppId: Long) {
            if(nanoAppId == NANO_APP_ID){
                Log.d(TAG, "Nanoapp loaded")
                updateScreenState()
                startRecognizer()
            }
        }

    }

    private val statusBarStateListener = object: StatusBarStateController.StateListener {
        override fun onDozingChanged(isDozing: Boolean) {
            handleDozingChanged(isDozing)
        }
    }

    private val wakefulnessLifecycleObserver = object: WakefulnessLifecycle.Observer {
        override fun onFinishedGoingToSleep() {
            handleWakefulnessChanged(false)
        }

        override fun onFinishedWakingUp() {
            handleWakefulnessChanged(true)
        }

        override fun onStartedGoingToSleep() {
            handleWakefulnessChanged(false)
        }

        override fun onStartedWakingUp() {
            handleWakefulnessChanged(false)
        }
    }

    private val featureVectorDumper = FeatureVectorDumper()
    private var isDozing = statusBarStateController.isDozing
    private var isAwake = wakefulnessLifecycle.getWakefulness() == 2
    private var isListening = false
    open var screenOn = isAwake && !isDozing
    private var screenStateUpdated = true
    private var contextHubClient: ContextHubClient? = null

    init {
        gestureConfiguration.setListener(object: GestureConfiguration.Listener {
            override fun onGestureConfigurationChanged(configuration: GestureConfiguration){
                updateSensitivity(configuration)
            }
        })
        statusBarStateController.addCallback(statusBarStateListener)
        wakefulnessLifecycle.addObserver(wakefulnessLifecycleObserver)
        initializeContextHubClientIfNull()
    }

    fun handleDozingChanged(isDozing: Boolean){
        if(this.isDozing != isDozing){
            this.isDozing = isDozing
            updateScreenState()
        }
    }

    open fun handleGestureDetection(gestureDetected: ColumbusGesture.GestureDetected){
        reportGestureDetected(protoGestureTypeToGesture(gestureDetected.gestureType), DetectionProperties(gestureDetected.gestureType == 2))
        featureVectorDumper.onGestureDetected(gestureDetected)
    }

    open fun handleNanoappEvents(nanoappEvents: ColumbusGesture.NanoAppEvents){
        //Not re-implemented as it's just logging
    }

    private fun handleWakefulnessChanged(awake: Boolean){
        if(isAwake != awake){
            isAwake = awake
            updateScreenState()
        }
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    open fun initializeContextHubClientIfNull(){
        if(contextHubClient == null){
            val contextHubManager = context.getSystemService("contexthub") as ContextHubManager
            val contextHub = contextHubManager.contextHubs.firstOrNull() ?: run {
                Log.e(TAG, "No context hubs found")
                return
            }
            contextHubClient = contextHubManager.createClient(contextHub, contextHubClientCallback)
        }
    }

    override fun isListening() = isListening

    private fun protoGestureTypeToGesture(protoGesture: Int): Int {
        return when(protoGesture){
            1, 2 -> protoGesture
            else -> 0
        }
    }

    @SuppressLint("MissingPermission")
    open fun sendMessageToNanoApp(messageType: Int, bytes: ByteArray, onFail: (() -> Unit)? = null, onSuccess: (() -> Unit)? = null){
        initializeContextHubClientIfNull()
        contextHubClient?.let {
            bgHandler.post {
                val nanoAppMessage = NanoAppMessage.createMessageToNanoApp(NANO_APP_ID, messageType, bytes)
                val result = it.sendMessageToNanoApp(nanoAppMessage)
                if(result != 0){
                    Log.e(TAG, "Unable to send message $messageType to nanoapp, error code $result")
                    onFail?.invoke()
                }else{
                    onSuccess?.invoke()
                }
            }
        } ?: run {
            Log.e(TAG, "ContextHubClient null")
        }
    }

    private fun sendScreenState(){
        val screenStateUpdate = ColumbusGesture.ScreenStateUpdate.newBuilder().apply {
            screenState = if(screenOn) 1 else 2
        }.build()
        val body = screenStateUpdate.toByteArray()
        sendMessageToNanoApp(400, body, {
            screenStateUpdated = false
        }, {
            screenStateUpdated = true
        })
    }

    override fun startListening(heuristicMode: Boolean){
        isListening = true
        startRecognizer()
        sendScreenState()
    }

    private fun startRecognizer(){
        val recognizerStart = ColumbusGesture.RecognizerStart.newBuilder().apply {
            sensitivity = gestureConfiguration.getSensitivity()
        }.build()
        val body = recognizerStart.toByteArray()
        sendMessageToNanoApp(100, body)
    }

    override fun stopListening() {
        sendMessageToNanoApp(101, ByteArray(0))
        isListening = false
    }

    protected fun updateScreenState(){
        val state = isAwake && !isDozing
        if(screenOn != state && !screenStateUpdated){
            screenOn = state
            if(isListening){
                sendScreenState()
            }
        }
    }

    private fun updateSensitivity(configuration: GestureConfiguration){
        val sensitivityUpdate = ColumbusGesture.SensitivityUpdate.newBuilder().apply {
            sensitivity = configuration.getSensitivity()
        }.build()
        val bytes = sensitivityUpdate.toByteArray()
        sendMessageToNanoApp(200, bytes)
    }

}