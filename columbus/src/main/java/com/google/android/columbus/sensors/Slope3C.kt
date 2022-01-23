package com.google.android.columbus.sensors

class Slope3C {

    private val _slopeX = Slope1C()
    private val _slopeY = Slope1C()
    private val _slopeZ = Slope1C()

    fun init(point: Point3f) {
        _slopeX.init(point.x)
        _slopeY.init(point.y)
        _slopeZ.init(point.z)
    }

    fun update(point: Point3f, d: Float): Point3f {
        return Point3f(
            _slopeX.update(point.x, d),
            _slopeY.update(point.y, d),
            _slopeZ.update(point.z, d)
        )
    }

}