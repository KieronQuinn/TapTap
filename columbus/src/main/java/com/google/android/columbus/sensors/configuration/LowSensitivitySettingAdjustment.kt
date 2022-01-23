package com.google.android.columbus.sensors.configuration

import android.content.Context
import com.google.android.columbus.ColumbusSettings

class LowSensitivitySettingAdjustment(
    context: Context,
    private val columbusSettings: ColumbusSettings,
    private val sensorConfiguration: SensorConfiguration
) : Adjustment(context) {

    private var useLowSensitivity = columbusSettings.useLowSensitivity

    private val settingsChangeListener = object: ColumbusSettings.ColumbusSettingsChangeListener {
        override fun onLowSensitivityChange(enabled: Boolean) {
            if(useLowSensitivity != enabled){
                useLowSensitivity = enabled
                onSensitivityChanged()
            }
        }
    }

    override fun adjustSensitivity(sensitivity: Float): Float {
        return if(useLowSensitivity) sensorConfiguration.lowSensitivityValue else sensitivity
    }

}