package com.google.android.columbus.sensors

import android.os.SystemClock
import android.util.Log
import android.util.SparseLongArray
import androidx.core.util.set
import com.kieronquinn.app.taptap.utils.logging.UiEventLogger

class GestureController(
    val gestureSensor: GestureSensor,
    private val uiEventLogger: UiEventLogger
) {

    companion object {
        private const val TAG = "Columbus/GestureControl"
    }

    interface GestureListener {
        fun onGestureDetected(sensor: GestureSensor, flags: Int, detectionProperties: GestureSensor.DetectionProperties)
    }

    private val lastTimestampMap = SparseLongArray()
    private var gestureListener: GestureListener? = null
    private val gestureSensorListener = object: GestureSensor.Listener {

        override fun onGestureDetected(
            sensor: GestureSensor,
            flags: Int,
            detectionProperties: GestureSensor.DetectionProperties
        ) {
            if(isThrottled(flags)){
                //Log.w(TAG, "Gesture $flags throttled")
                return
            }

            if(flags == 1){
                //TODO log
            }

            gestureListener?.onGestureDetected(sensor, flags, detectionProperties)
        }
    }

    init {
        gestureSensor.setGestureListener(gestureSensorListener)
    }

    private fun isThrottled(flags: Int): Boolean {
        val uptime = SystemClock.uptimeMillis()
        val lastTimestamp = lastTimestampMap[flags]
        lastTimestampMap.set(flags, uptime)
        return uptime - lastTimestamp <= 500L
    }

    fun setGestureListener(gestureListener: GestureListener) {
        this.gestureListener = gestureListener
    }

    fun startListening(): Boolean {
        return if(!gestureSensor.isListening()){
            gestureSensor.startListening(false)
            true
        }else false
    }

    fun stopListening(): Boolean {
        return if(gestureSensor.isListening()){
            gestureSensor.stopListening()
            true
        }else false
    }

}