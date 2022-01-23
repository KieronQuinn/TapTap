package com.kieronquinn.app.taptap.components.columbus.sensors

import android.content.Context
import android.content.res.AssetManager
import android.os.SystemClock
import com.google.android.columbus.sensors.TapRT
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import org.koin.core.scope.Scope

/**
 *  Extension of [TapRT] which implements triple tap
 */
class TapTapTapRT(
    context: Context,
    mSizeWindowNs: Long,
    assetManager: AssetManager,
    private val isTripleTapEnabled: Boolean,
    private val sensitivity: Float,
    tapModel: TapModel,
    scope: Scope,
    settings: TapTapSettings
) : TapRT(mSizeWindowNs) {

    companion object {
        const val mMaxTimeGapTripleNs = 750000000L
    }

    init {
        _tflite = TapTapTfClassifier(assetManager, tapModel, scope, settings)
    }

    override fun checkDoubleTapTiming(timestamp: Long): Int {
        if (!isTripleTapEnabled) {
            return super.checkDoubleTapTiming(timestamp)
        }
        val firstPassIterator = _tBackTapTimestamps.iterator()
        while (firstPassIterator.hasNext()) {
            val pastTimestamp = firstPassIterator.next()
            if (timestamp - pastTimestamp <= mMaxTimeGapTripleNs) {
                continue
            }
            firstPassIterator.remove()
        }

        if (_tBackTapTimestamps.isEmpty()) {
            return 0
        }

        var tapCount = 0
        val secondPassIterator = _tBackTapTimestamps.iterator()
        val timeNow = SystemClock.elapsedRealtimeNanos()
        while (secondPassIterator.hasNext()) {
            val pastTimestamp = secondPassIterator.next()
            if (_tBackTapTimestamps.last() - pastTimestamp <= mMinTimeGapNs) {
                continue
            }
            tapCount++
        }

        if (tapCount >= 3 || timeNow.minus(_tBackTapTimestamps.first()) > mMaxTimeGapTripleNs) {
            _tBackTapTimestamps.clear()
            if (tapCount == 1) {
                return 2
            } else if (tapCount >= 2) {
                return 3
            }
        }

        return 1
    }


    override fun reset(justClearFv: Boolean) {
        getPositivePeakDetector().setMinNoiseTolerate(sensitivity)
        super.reset(justClearFv)
    }

}