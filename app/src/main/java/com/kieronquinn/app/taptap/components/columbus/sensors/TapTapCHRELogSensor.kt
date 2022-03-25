package com.kieronquinn.app.taptap.components.columbus.sensors

import android.os.SystemClock
import android.util.Log
import com.google.android.columbus.sensors.GestureSensor
import com.google.android.columbus.sensors.TapRT
import com.kieronquinn.app.taptap.components.columbus.sensors.ServiceEventEmitter.ServiceEvent
import com.kieronquinn.app.taptap.utils.extensions.runOnClose
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.core.scope.Scope
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class TapTapCHRELogSensor(
    internal val scope: Scope,
    private val isTripleTapEnabled: Boolean,
    private val serviceEventEmitter: ServiceEventEmitter
): GestureSensor() {

    private val lifecycleScope = MainScope()
    private val tapEvents = ArrayDeque<Long>()
    private val isListening = MutableStateFlow(false)

    companion object {
        private const val mMaxTimeGapTripleNs = 1000000000L
        private val LOGCAT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private const val COLUMBUS_LOG_MESSAGE_FOOTER = "[COLUMBUS] back tap detected!"
        private const val COLUMBUS_LOG_TAG = "CHRE"
    }

    private val logcatLines = callbackFlow {
        val time = LOGCAT_TIME_FORMAT.format(LocalDateTime.now()) + ".000"
        val command = arrayOf("logcat", "-s", COLUMBUS_LOG_TAG, "-T", time)
        var process: Process? = null
        val processThread = Thread {
            process = ProcessBuilder(*command).start().also {
                it.inputStream.reader().useLines { lines ->
                    lines.forEach { line ->
                        trySend(Pair(SystemClock.elapsedRealtimeNanos(), line.trim()))
                    }
                }
            }
        }
        processThread.start()
        awaitClose {
            process?.destroy()
            processThread.interrupt()
        }
    }.flowOn(Dispatchers.IO)

    private val columbusTimestamps = logcatLines.mapNotNull {
        if(it.second.endsWith(COLUMBUS_LOG_MESSAGE_FOOTER)) it.first else null
    }

    @Synchronized
    fun handleTapEvents(
        newTimestamps: List<Long>
    ) {
        val timeout = TapRT.mMaxTimeGapNs
        val uptime = SystemClock.elapsedRealtimeNanos()
        tapEvents.addAll(newTimestamps)
        tapEvents.removeIf { uptime - it > timeout }
        if (tapEvents.size >= 2) {
            reportGestureDetected(1, false)
            tapEvents.clear()
            return
        }
        reportGestureDetected(2, true)
    }

    @Synchronized
    fun handleTapEventsTriple(
        newTimestamps: List<Long>,
        handleDoubleTap: Boolean = false,
        handleTripleTap: Boolean = true
    ) {
        val timeout = mMaxTimeGapTripleNs
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

    private var tripleTapDelay: Job? = null

    private fun delayDoubleTapCheck(delayBy: Long) {
        clearDoubleTapCheck()
        tripleTapDelay = GlobalScope.launch {
            delay(delayBy)
            handleTapEventsTriple(emptyList(), handleDoubleTap = true, handleTripleTap = false)
            tripleTapDelay = null
        }
    }

    private fun clearDoubleTapCheck() {
        tripleTapDelay?.cancel()
    }

    private fun reportGestureDetected(flags: Int, isHapticConsumed: Boolean) {
        Log.d("TTC", "reportGestureDetected from log sensor")
        reportGestureDetected(flags, DetectionProperties(isHapticConsumed))
    }

    private fun setupTaps() = lifecycleScope.launch(Dispatchers.IO) {
        columbusTimestamps.onStart {
            serviceEventEmitter.postServiceEvent(ServiceEvent.Started)
        }.collect {
            if(isTripleTapEnabled){
                handleTapEventsTriple(listOf(it))
            }else{
                handleTapEvents(listOf(it))
            }
        }
    }

    init {
        scope.runOnClose {
            lifecycleScope.cancel()
        }
        setupTaps()
    }

    override fun isListening(): Boolean {
        return isListening.value
    }

    override fun startListening(heuristicMode: Boolean) {
        lifecycleScope.launch {
            isListening.emit(true)
        }
    }

    override fun stopListening() {
        lifecycleScope.launch {
            isListening.emit(false)
        }
    }

}