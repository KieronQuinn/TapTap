package com.kieronquinn.app.shared.taprt

interface BaseTapRT {

    fun updateData(type: Int, lastX: Float, lastY: Float, lastZ: Float, lastT: Long, interval: Long, isHeuristic: Boolean)
    fun checkDoubleTapTiming(timestamp: Long): Int
    fun reset(justClearFv: Boolean)

}