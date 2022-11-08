package com.samsung.android.backtap

class Slope {

    private val result = Point3f(0.0f, 0.0f, 0.0f)
    private var xRawLast = 0f
    private var yRawLast = 0f
    private var zRawLast = 0f

    fun init(point3f0: Point3f) {
        xRawLast = point3f0.x
        yRawLast = point3f0.y
        zRawLast = point3f0.z
    }

    fun update(point3f0: Point3f, f: Float): Point3f {
        val f1 = point3f0.x * f
        val f2 = f1 - xRawLast
        xRawLast = f1
        val f3 = point3f0.y * f
        val f4 = f3 - yRawLast
        yRawLast = f3
        val f5 = point3f0.z * f
        val f6 = f5 - zRawLast
        zRawLast = f5
        result.x = f2
        result.y = f4
        result.z = f6
        return result
    }

}