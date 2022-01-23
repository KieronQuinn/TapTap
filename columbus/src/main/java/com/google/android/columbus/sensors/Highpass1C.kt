package com.google.android.columbus.sensors

class Highpass1C {

    private var para = 1.0f
    private var xLast = 0.0f
    private var yLast = 0.0f

    fun init(value: Float) {
        xLast = value
        yLast = value
    }

    fun setPara(para: Float) {
        this.para = para
    }

    fun update(value: Float): Float {
        val newYLast = (value - xLast) * para + yLast * para
        yLast = newYLast
        xLast = value
        return newYLast
    }
}