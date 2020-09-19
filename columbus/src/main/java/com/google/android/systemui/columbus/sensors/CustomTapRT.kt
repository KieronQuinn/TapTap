package com.google.android.systemui.columbus.sensors

import android.content.res.AssetManager
import android.os.SystemClock
import android.util.Log

class CustomTapRT(long: Long, assetManager: AssetManager, deviceName: String) : TapRT(long, assetManager, deviceName) {

    public var isTripleTapEnabled = true
    public var TRIPLE_TAP_DELAY = 750000000L

    override fun checkDoubleTapTiming(arg6: Long): Int {
        if(isTripleTapEnabled) return checkTripleTapTiming(arg6)
        return super.checkDoubleTapTiming(arg6)
    }

    private fun checkTripleTapTiming(arg6: Long): Int {
        val v0 = _tBackTapTimestamps.iterator()
        while (v0.hasNext()) {
            val v1 = v0.next()!!
            if (arg6 - v1 <= 750000000L) {
                continue
            }
            v0.remove()
        }

        if (_tBackTapTimestamps.isEmpty()) {
            return 0
        }

        var tapCount = 0
        val v6: Iterator<*> = _tBackTapTimestamps.iterator()
        val timeNow = SystemClock.elapsedRealtimeNanos()
        while (v6.hasNext()) {
            val v7 = v6.next()!!
            if (_tBackTapTimestamps.last as Long - v7 as Long <= 100000000L) {
                continue
            }
            tapCount++
        }

        //Skip waiting if we've already got 3 taps (speeds up really fast tap response time)
        if(tapCount >= 3 || (timeNow.minus(_tBackTapTimestamps.first as Long)) > TRIPLE_TAP_DELAY){
            _tBackTapTimestamps.clear()
            if(tapCount == 1){
                return 2
            }else if(tapCount >= 2){
                //Although this includes tap counts of 4 or more, we'll include them too to allow for people spam tapping
                return 3
            }
        }

        return 1
    }

}