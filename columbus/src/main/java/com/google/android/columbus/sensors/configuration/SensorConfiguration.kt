package com.google.android.columbus.sensors.configuration

import android.content.Context
import com.google.android.columbus.R

class SensorConfiguration(context: Context) {

    val defaultSensitivityValue = context.resources.getInteger(R.integer.columbus_default_sensitivity_percent) * 0.01f
    val lowSensitivityValue = context.resources.getInteger(R.integer.columbus_low_sensitivity_percent) * 0.01f

}