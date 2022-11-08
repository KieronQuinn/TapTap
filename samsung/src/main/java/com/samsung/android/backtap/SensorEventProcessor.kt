package com.samsung.android.backtap

class SensorEventProcessor(
    var sensitivity: Float
) {

    private var _gotAcc = false
    private var _gotGyro = false
    private val _highpassAcc = HighPassFilter()
    private val _highpassGyro = HighPassFilter()
    private val _resampleAcc = Resample()
    private val _resampleGyro = Resample()
    private val _slopeAcc = Slope()
    private val _slopeGyro = Slope()
    private var _syncTime = 0L

    init {
        _highpassAcc.setPara(0.05f)
        _highpassGyro.setPara(0.05f)
    }

    private fun checkIfPeak(f: Float, f1: Float): Boolean {
        val multiplied = f1 * sensitivity
        val f2 = (f - multiplied).toDouble()
        if ((slope * f2).compareTo(0.0) < 0) {
            slope = f2
            val f3 = az.std * 3.0f
            return multiplied >= az.mean + f3 && multiplied.toDouble() >= 0.05f
        }
        slope = f2
        return false
    }

    private fun processAccAndKeySignal() {
        val point3f0 = _resampleAcc.results.point
        val v = _resampleAcc.results.t
        val point3f1 = _slopeAcc.update(point3f0, 2500000.0f / _resampleAcc.interval.toFloat())
        val point3f2 = _highpassAcc.update(point3f1)
        val f = az.newestElement
        if (az.checkIfFull()) {
            ax.dequeue()
            ay.dequeue()
            az.dequeue(true)
        }
        val f1 = point3f2.x
        ax.enqueue(f1)
        val f2 = point3f2.y
        ay.enqueue(f2)
        val f3 = point3f2.z
        az.enqueue(f3, true)
        if (hasMajorPeak) {
            ++rightCount
            if (checkIfPeak(point3f2.z, f)) {
                ++nMinorPeaks
            }
            return
        }
        val z = checkIfPeak(point3f2.z, f)
        hasMajorPeak = z
        if (z) {
            timestamp = v
        }
    }

    fun processGyro() {
        val point3f0 = _resampleGyro.results.point
        val point3f1 = _slopeGyro.update(point3f0, 2500000.0f / _resampleGyro.interval.toFloat())
        val point3f2 = _highpassGyro.update(point3f1)
        if (gz.checkIfFull()) {
            gx.dequeue()
            gy.dequeue()
            gz.dequeue()
        }
        val f = point3f2.x
        gx.enqueue(f)
        val f1 = point3f2.y
        gy.enqueue(f1)
        val f2 = point3f2.z
        gz.enqueue(f2)
    }

    fun reset() {
        _syncTime = 0L
        _gotAcc = false
        _gotGyro = false
        az.reset()
        ay.reset()
        ax.reset()
        gz.reset()
        gy.reset()
        gx.reset()
        hasMajorPeak = false
        nMinorPeaks = 0
        slope = 0.0
        rightCount = 0
        lastWindowEnd = 0L
    }

    fun updateData(v: Int, f: Float, f1: Float, f2: Float, v1: Long, v2: Long) {
        when (v) {
            1 -> {
                _gotAcc = true
                if (_syncTime == 0L) {
                    _resampleAcc.init(f, f1, f2, v1, v2)
                }
                if (!_gotGyro) {
                    return
                }
            }
            4 -> {
                _gotGyro = true
                if (_syncTime == 0L) {
                    _resampleGyro.init(f, f1, f2, v1, v2)
                }
                if (!_gotAcc) {
                    return
                }
            }
        }
        if (0L == _syncTime) {
            _syncTime = v1
            _resampleAcc.setSyncTime(v1)
            _resampleGyro.setSyncTime(_syncTime)
            _slopeAcc.init(_resampleAcc.results.point)
            _slopeGyro.init(_resampleGyro.results.point)
            _highpassAcc.init(Point3f(0.0f, 0.0f, 0.0f))
            _highpassGyro.init(Point3f(0.0f, 0.0f, 0.0f))
            return
        }
        if (v == 1) {
            while (_resampleAcc.update(f, f1, f2, v1)) {
                processAccAndKeySignal()
                if (rightCount < 24) {
                    continue
                }
                return
            }
        } else if (v == 4) {
            while (_resampleGyro.update(f, f1, f2, v1)) {
                processGyro()
            }
        }
    }

    companion object {
        var ax = DataQueue()
        var ay = DataQueue()
        var az = DataQueue()
        var gx = DataQueue()
        var gy = DataQueue()
        var gz = DataQueue()
        var hasMajorPeak = false
        var lastWindowEnd = 0L
        var nMinorPeaks = 0
        var rightCount = 0
        var slope = 0.0
        var timestamp: Long = 0
    }
}