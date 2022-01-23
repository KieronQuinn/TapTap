package com.google.android.columbus.feedback

import com.google.android.columbus.sensors.GestureSensor

interface FeedbackEffect {
    fun onGestureDetected(flags: Int, detectionProperties: GestureSensor.DetectionProperties?)
}