package com.kieronquinn.app.taptap.models.gate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R

enum class TapTapGateCategory(@StringRes val labelRes: Int, @StringRes val descRes: Int, @DrawableRes val icon: Int) {
    DEVICE(R.string.add_gate_category_device, R.string.add_gate_category_device_desc, R.drawable.ic_gate_category_device),
    SENSORS(R.string.add_gate_category_sensors, R.string.add_gate_category_sensors_desc, R.drawable.ic_gate_category_sensors),
    AUDIO(R.string.add_gate_category_audio, R.string.add_gate_category_audio_desc, R.drawable.ic_gate_category_audio),
    EVENTS(R.string.add_gate_category_events, R.string.add_gate_category_events_desc, R.drawable.ic_gate_category_events)
}