package com.google.android.columbus.sensors

import android.util.Log
import java.util.*

abstract class GestureSensor: Sensor {

    inner class DetectionProperties(var isHapticConsumed: Boolean) {
        val actionId = Random().nextLong()
    }

    interface Listener {
        fun onGestureDetected(sensor: GestureSensor, flags: Int, detectionProperties: DetectionProperties)
    }

    private var listener: Listener? = null

    fun reportGestureDetected(flags: Int, detectionProperties: DetectionProperties){
        listener?.onGestureDetected(this, flags, detectionProperties)
    }

    fun setGestureListener(listener: Listener?){
        this.listener = listener
    }

}