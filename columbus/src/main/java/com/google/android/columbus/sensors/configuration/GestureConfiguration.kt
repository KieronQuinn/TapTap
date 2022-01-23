package com.google.android.columbus.sensors.configuration

import android.util.Range
import kotlin.math.abs

class GestureConfiguration(
    private val adjustments: List<Adjustment>,
    private val sensorConfiguration: SensorConfiguration
) {

    interface Listener {
        fun onGestureConfigurationChanged(configuration: GestureConfiguration)
    }

    companion object {
        private val SENSITIVITY_RANGE = Range.create(0f, 1f)
    }

    private var sensitivity = sensorConfiguration.defaultSensitivityValue
    private var listener: Listener? = null

    init {
        adjustments.forEach {
            it.setCallback(this::adjustmentCallback)
        }
        updateSensitivity()
    }

    private fun adjustmentCallback(adjustment: Adjustment){
        updateSensitivity()
    }

    fun getSensitivity(): Float {
        return sensitivity
    }

    fun setListener(listener: Listener?){
        this.listener = listener
    }

    fun updateSensitivity() {
        var newValue = sensorConfiguration.defaultSensitivityValue
        adjustments.forEach {
            val sensitivity = it.adjustSensitivity(newValue)
            val clampedSensitivity = SENSITIVITY_RANGE.clamp(sensitivity)
            newValue = clampedSensitivity
        }
        if(abs(sensitivity - newValue) >= 0.05f){
            sensitivity = newValue
            listener?.onGestureConfigurationChanged(this)
        }
    }

}