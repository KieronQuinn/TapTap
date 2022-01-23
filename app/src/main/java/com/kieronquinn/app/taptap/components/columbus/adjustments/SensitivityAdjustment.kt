package com.kieronquinn.app.taptap.components.columbus.adjustments

import android.content.Context
import com.google.android.columbus.sensors.configuration.Adjustment
import com.kieronquinn.app.taptap.components.settings.TapTapSettings

class SensitivityAdjustment(context: Context, private val settings: TapTapSettings) :
    Adjustment(context) {

    init {
        onSensitivityChanged()
    }

    override fun adjustSensitivity(sensitivity: Float): Float {
        val lowSensitivity = settings.columbusCHRELowSensitivity.getSync()
        return if(lowSensitivity) {
            0f
        }else sensitivity
    }

}