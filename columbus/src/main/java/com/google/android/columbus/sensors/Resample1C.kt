package com.google.android.columbus.sensors

open class Resample1C {

    protected var tInterval = 0L
    protected var tRawLast: Long = 0
    protected var tResampledLast: Long = 0
    protected var xRawLast = 0f
    protected var xResampledThis = 0.0f

    fun init(x: Float, t: Long, interval: Long) {
        xRawLast = x
        tRawLast = t
        xResampledThis = x
        tResampledLast = t
        tInterval = interval
    }

    fun getInterval(): Long {
        return tInterval
    }

    fun setSyncTime(arg1: Long) {
        tResampledLast = arg1
    }

}