package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R

enum class TapGateCategory(@StringRes val labelRes: Int, @DrawableRes val icon: Int) {
    DEVICE(R.string.add_gate_category_device, R.drawable.ic_gate_category_device),
    SENSORS(R.string.add_gate_category_sensors, R.drawable.ic_gate_category_sensors),
    AUDIO(R.string.add_gate_category_audio, R.drawable.ic_action_volume_up),
    EVENTS(R.string.add_gate_category_events, R.drawable.ic_app_showing)
}