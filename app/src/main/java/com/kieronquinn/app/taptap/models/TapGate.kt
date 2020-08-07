package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.systemui.columbus.gates.*
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.gates.*
import com.kieronquinn.app.taptap.columbus.gates.CameraVisibility

enum class TapGate(val clazz: Class<*>, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, @StringRes val formattableDescription: Int? = null, val dataType: GateDataTypes? = null) {
    POWER_STATE(PowerState::class.java, R.string.gate_power_state, R.string.gate_power_state_desc, R.drawable.ic_power_state),
    POWER_STATE_INVERSE(PowerStateInverse::class.java, R.string.gate_power_state_inverse, R.string.gate_power_state_inverse_desc, R.drawable.ic_gate_power_state_inverse),
    CHARGING_STATE(ChargingState::class.java, R.string.gate_charging_state, R.string.gate_charging_state_desc, R.drawable.ic_charging_state),
    USB_STATE(UsbState::class.java, R.string.gate_usb_state, R.string.gate_usb_state_desc, R.drawable.ic_usb_state),
    CAMERA_VISIBILITY(CameraVisibility::class.java, R.string.gate_camera_visibility, R.string.gate_camera_visibility_desc, R.drawable.ic_camera_visibility),
    TELEPHONY_ACTIVITY(TelephonyActivity::class.java, R.string.gate_telephony_activity, R.string.gate_telephony_activity_desc, R.drawable.ic_telephony_activity),
    APP_SHOWING(AppVisibility::class.java, R.string.gate_app_showing, R.string.gate_app_showing_desc, R.drawable.ic_app_showing, R.string.gate_app_showing_desc_formatted, GateDataTypes.PACKAGE_NAME),
    KEYBOARD_VISIBILITY(KeyboardVisibility::class.java, R.string.gate_keyboard_visibility, R.string.gate_keyboard_visibility_desc, R.drawable.ic_gate_keyboard_visibility),
    ORIENTATION_LANDSCAPE(Orientation::class.java, R.string.gate_orientation_landscape, R.string.gate_orientation_landscape_desc, R.drawable.ic_gate_orientation),
    ORIENTATION_PORTRAIT(Orientation::class.java, R.string.gate_orientation_portrait, R.string.gate_orientation_portrait_desc, R.drawable.ic_gate_orientation)
}

enum class GateDataTypes {
    PACKAGE_NAME
}