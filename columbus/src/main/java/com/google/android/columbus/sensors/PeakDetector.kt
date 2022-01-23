package com.google.android.columbus.sensors

class PeakDetector {

    private var _amplitudeMajorPeak = 0.0f
    private var _amplitudeReference = 0.0f
    private var _idMajorPeak: Int = -1
    private var _minNoiseTolerate = 0.0f
    private var _noiseTolerate = 0f
    private var _windowSize = 0

    fun setMinNoiseTolerate(noise: Float) {
        _minNoiseTolerate = noise
    }

    fun setWindowSize(size: Int) {
        _windowSize = size
    }

    fun update(value: Float) {
        val adjustedPeak = _idMajorPeak - 1
        _idMajorPeak = adjustedPeak
        if (adjustedPeak < 0) {
            _amplitudeMajorPeak = 0.0f
        }
        _noiseTolerate = _minNoiseTolerate
        val amplitudeMajorPeak = _amplitudeMajorPeak
        val adjustedMajorPeak = amplitudeMajorPeak / 5.0f
        if (adjustedMajorPeak > _minNoiseTolerate) {
            _noiseTolerate = adjustedMajorPeak
        }
        val adjustedReference = _amplitudeReference - value
        val noiseTolerate = _noiseTolerate
        if (adjustedReference >= noiseTolerate) {
            _amplitudeReference = value
        } else if (adjustedReference < 0.0f && value > noiseTolerate) {
            _amplitudeReference = value
            if (value > amplitudeMajorPeak) {
                _idMajorPeak = _windowSize - 1
                _amplitudeMajorPeak = value
                return
            }
        }
    }

    fun getIdMajorPeak(): Int {
        return _idMajorPeak
    }

}