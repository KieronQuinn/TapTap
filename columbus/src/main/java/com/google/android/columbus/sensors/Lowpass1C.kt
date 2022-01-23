package com.google.android.columbus.sensors

class Lowpass1C {

    private var para = 1.0f
    private var xLast = 0.0f

    fun init(arg1: Float) {
        xLast = arg1
    }

    fun setPara(arg1: Float) {
        para = arg1
    }

    fun update(value: Float): Float {
        val newXLast = value * para + (1.0f - para) * xLast
        xLast = newXLast
        return newXLast
    }
}