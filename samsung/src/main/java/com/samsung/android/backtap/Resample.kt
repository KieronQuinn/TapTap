package com.samsung.android.backtap

class Resample {

    private val sample = Sample(0.0f, 0.0f, 0.0f, 0L)
    private var tRawLast = 0L
    private var tResampledLast = 0L
    private var xRawLast = 0.0f
    private var xResampledThis = 0.0f
    private var yRawLast = 0.0f
    private var yResampledThis = 0.0f
    private var zRawLast = 0.0f
    private var zResampledThis = 0.0f

    var interval = 0L
        private set

    val results: Sample
        get() {
            sample.setX(xResampledThis)
            sample.setY(yResampledThis)
            sample.setZ(zResampledThis)
            sample.t = tResampledLast
            return sample
        }

    fun init(f: Float, f1: Float, f2: Float, v: Long, v1: Long) {
        xRawLast = f
        tRawLast = v
        xResampledThis = f
        tResampledLast = v
        interval = v1
        yRawLast = f1
        zRawLast = f2
        yResampledThis = f1
        zResampledThis = f2
    }

    fun setSyncTime(v: Long) {
        tResampledLast = v
    }

    fun update(f: Float, f1: Float, f2: Float, v: Long): Boolean {
        val v1 = tRawLast
        if (v.compareTo(v1) != 0) {
            val v2 = (if (interval > 0L) interval else v - v1) + tResampledLast
            if (v < v2) {
                tRawLast = v
                xRawLast = f
                yRawLast = f1
                zRawLast = f2
                return false
            }
            val f3 = (v2 - v1).toFloat() / (v - v1).toFloat()
            xResampledThis = xRawLast + (f - xRawLast) * f3
            yResampledThis = yRawLast + (f1 - yRawLast) * f3
            zResampledThis = f3 * (f2 - zRawLast) + zRawLast
            tResampledLast = v2
            if (v1 < v2) {
                tRawLast = v
                xRawLast = f
                yRawLast = f1
                zRawLast = f2
            }
            return true
        }
        return false
    }
}