package com.google.android.columbus.sensors

class Highpass3C {

    private val xHighpass = Highpass1C()
    private val yHighpass = Highpass1C()
    private val zHighpass = Highpass1C()

    fun init(point: Point3f) {
        xHighpass.init(point.x)
        yHighpass.init(point.y)
        zHighpass.init(point.z)
    }

    fun setPara(para: Float) {
        xHighpass.setPara(para)
        yHighpass.setPara(para)
        zHighpass.setPara(para)
    }

    fun update(point: Point3f): Point3f {
        return Point3f(xHighpass.update(point.x), yHighpass.update(point.y), zHighpass.update(point.z))
    }

}