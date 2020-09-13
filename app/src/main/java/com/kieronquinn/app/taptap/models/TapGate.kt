package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.systemui.columbus.gates.*
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.gates.*
import com.kieronquinn.app.taptap.columbus.gates.CameraVisibility
import com.kieronquinn.app.taptap.columbus.gates.TelephonyActivity

/*
    Tap Gates (wrapping the Gate class) contain details on how the gate will be shown in the settings
    clazz: The class name of the actual Gate that will be run. This is not currently used, but may be used for verification later down the line
    nameRes: The String resource to use for the name/title of the gate
    descriptionRes: The String resource to use for the description of the gate
    iconRes: The Drawable resource to use for the icon of the gate
    whenDescriptionRes: The String resource to use for the description of the gate when it is being added as a "when gate" (or requirement) for an action
    formattableDescription (optional) : The String resource for a description of this gate that can be formatted to contain the gate's data, such as an app name for the App Showing gate
    dataType (optional): The kind of data that is stored in the 'data' field of GateInternal for this action. This will be used to open pickers in the UI after a gate is added, for example an app picker. See GateDataTypes for a full list
 */
enum class TapGate(val clazz: Class<*>, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, @StringRes val whenDescriptionRes: Int, @StringRes val formattableDescription: Int? = null, val dataType: GateDataTypes? = null) {
    POWER_STATE(PowerState::class.java, R.string.gate_power_state, R.string.gate_power_state_desc, R.drawable.ic_power_state, R.string.gate_power_state_desc_when),
    POWER_STATE_INVERSE(PowerStateInverse::class.java, R.string.gate_power_state_inverse, R.string.gate_power_state_inverse_desc, R.drawable.ic_gate_power_state_inverse, R.string.gate_power_state_inverse_desc_when),
    CHARGING_STATE(ChargingState::class.java, R.string.gate_charging_state, R.string.gate_charging_state_desc, R.drawable.ic_charging_state, R.string.gate_charging_state_desc_when),
    USB_STATE(UsbState::class.java, R.string.gate_usb_state, R.string.gate_usb_state_desc, R.drawable.ic_usb_state, R.string.gate_usb_state_desc_when),
    CAMERA_VISIBILITY(CameraVisibility::class.java, R.string.gate_camera_visibility, R.string.gate_camera_visibility_desc, R.drawable.ic_camera_visibility, R.string.gate_camera_visibility_desc_when),
    TELEPHONY_ACTIVITY(TelephonyActivity::class.java, R.string.gate_telephony_activity, R.string.gate_telephony_activity_desc, R.drawable.ic_telephony_activity, R.string.gate_telephony_activity_desc_when),
    APP_SHOWING(AppVisibility::class.java, R.string.gate_app_showing, R.string.gate_app_showing_desc, R.drawable.ic_app_showing, R.string.gate_app_showing_desc_when, R.string.gate_app_showing_desc_formatted, GateDataTypes.PACKAGE_NAME),
    KEYBOARD_VISIBILITY(KeyboardVisibility::class.java, R.string.gate_keyboard_visibility, R.string.gate_keyboard_visibility_desc, R.drawable.ic_gate_keyboard_visibility, R.string.gate_keyboard_visibility_desc_when),
    ORIENTATION_LANDSCAPE(Orientation::class.java, R.string.gate_orientation_landscape, R.string.gate_orientation_landscape_desc, R.drawable.ic_gate_orientation, R.string.gate_orientation_landscape_desc_when),
    ORIENTATION_PORTRAIT(Orientation::class.java, R.string.gate_orientation_portrait, R.string.gate_orientation_portrait_desc, R.drawable.ic_gate_orientation, R.string.gate_orientation_portrait_desc_when),
    TABLE(TableDetection::class.java, R.string.gate_table, R.string.gate_table_desc, R.drawable.ic_gate_table, R.string.gate_table_desc_when),
    POCKET(PocketDetection::class.java, R.string.gate_pocket, R.string.gate_pocket_desc, R.drawable.ic_gate_pocket, R.string.gate_pocket_desc_when),
    HEADSET(Headset::class.java, R.string.gate_headset, R.string.gate_headset_desc, R.drawable.ic_gate_headset_inverse, R.string.gate_headset_desc_when),
    HEADSET_INVERSE(HeadsetInverse::class.java, R.string.gate_headset_inverse, R.string.gate_headset_desc_inverse, R.drawable.ic_gate_headset, R.string.gate_headset_desc_when_inverse),
    MUSIC(Music::class.java, R.string.gate_music, R.string.gate_music_desc, R.drawable.ic_gate_music, R.string.gate_music_desc_when),
    MUSIC_INVERSE(MusicInverse::class.java, R.string.gate_music_inverse, R.string.gate_music_desc_inverse, R.drawable.ic_gate_music_inverse, R.string.gate_music_desc_when_inverse)
}

enum class GateDataTypes {
    PACKAGE_NAME
}