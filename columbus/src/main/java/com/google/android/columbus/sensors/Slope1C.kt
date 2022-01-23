package com.google.android.columbus.sensors

class Slope1C {

    private var xDelta = 0.0f
    private var xRawLast = 0f

    fun init(x: Float) {
        xRawLast = x
    }

    fun update(value: Float, d: Float): Float {
        val x = value * d
        val delta = x - xRawLast
        xDelta = delta
        xRawLast = x
        return delta
    }
}