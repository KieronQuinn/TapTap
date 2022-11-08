package com.samsung.android.backtap

class HighPassFilter {

    private var para = 1.0f
    private val result = Point3f(0.0f, 0.0f, 0.0f)
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f
    private var z1 = 0f
    private var z2 = 0f

    fun init(point3f0: Point3f) {
        x1 = point3f0.x
        x2 = point3f0.x
        y1 = point3f0.y
        y2 = point3f0.y
        z1 = point3f0.z
        z2 = point3f0.z
    }

    fun setPara(f: Float) {
        para = f
    }

    fun update(point3f0: Point3f): Point3f {
        val f = point3f0.x
        x2 = (f - x1) * para + x2 * para
        x1 = f
        val f1 = point3f0.y
        y2 = (f1 - y1) * para + y2 * para
        y1 = f1
        val f2 = point3f0.z
        z2 = (f2 - z1) * para + z2 * para
        z1 = f2
        result.x = x2
        result.y = y2
        result.z = z2
        return result
    }
}