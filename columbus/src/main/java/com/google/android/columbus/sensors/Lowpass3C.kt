package com.google.android.columbus.sensors

class Lowpass3C {

    private val xLowpass = Lowpass1C()
    private val yLowpass = Lowpass1C()
    private val zLowpass = Lowpass1C()

    fun init(point: Point3f) {
        xLowpass.init(point.x)
        yLowpass.init(point.y)
        zLowpass.init(point.z)
    }

    fun setPara(para: Float) {
        xLowpass.setPara(para)
        yLowpass.setPara(para)
        zLowpass.setPara(para)
    }

    fun update(point: Point3f): Point3f {
        return Point3f(xLowpass.update(point.x), yLowpass.update(point.y), zLowpass.update(point.z))
    }

}