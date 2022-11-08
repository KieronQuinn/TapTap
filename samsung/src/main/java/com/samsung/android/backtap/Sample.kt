package com.samsung.android.backtap

class Sample(f: Float, f1: Float, f2: Float, v: Long) {

    var point: Point3f
    var t: Long

    init {
        val point3f0 = Point3f(0.0f, 0.0f, 0.0f)
        point = point3f0
        t = v
        point3f0.x = f
        point.y = f1
        point.z = f2
    }

    fun setX(f: Float) {
        point.x = f
    }

    fun setY(f: Float) {
        point.y = f
    }

    fun setZ(f: Float) {
        point.z = f
    }

}