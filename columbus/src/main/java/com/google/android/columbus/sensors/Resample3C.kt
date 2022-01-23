package com.google.android.columbus.sensors

class Resample3C : Resample1C() {

    private var yRawLast = 0f
    private var yResampledThis = 0f
    private var zRawLast = 0f
    private var zResampledThis = 0f

    val results: Sample3C
        get() = Sample3C(xResampledThis, yResampledThis, zResampledThis, tResampledLast)

    fun init(x: Float, y: Float, z: Float, t: Long, interval: Long) {
        this.init(x, t, interval)
        yRawLast = y
        zRawLast = z
        yResampledThis = y
        zResampledThis = z
    }

    fun update(x: Float, y: Float, z: Float, t: Long): Boolean {
        val tLast = tRawLast
        if (t.compareTo(tLast) != 0) {
            val interval = (if (this.tInterval > 0L) this.tInterval else t - tLast) + tResampledLast
            if (t < interval) {
                tRawLast = t
                xRawLast = x
                yRawLast = y
                zRawLast = z
                return false
            }
            val tLastSecond = tRawLast
            val scaledInterval = (interval - tLastSecond).toFloat() / (t - tLastSecond).toFloat()
            xResampledThis = xRawLast + (x - xRawLast) * scaledInterval
            yResampledThis = yRawLast + (y - yRawLast) * scaledInterval
            zResampledThis = scaledInterval * (z - zRawLast) + zRawLast
            tResampledLast = interval
            if (tLastSecond < interval) {
                tRawLast = t
                xRawLast = x
                yRawLast = y
                zRawLast = z
            }
            return true
        }
        return false
    }
}