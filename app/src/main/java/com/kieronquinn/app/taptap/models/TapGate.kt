package com.kieronquinn.app.taptap.models

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.systemui.columbus.gates.*
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.columbus.gates.*
import com.kieronquinn.app.taptap.core.columbus.gates.CameraVisibility
import com.kieronquinn.app.taptap.core.columbus.gates.TelephonyActivity
import com.kieronquinn.app.taptap.utils.SidecarProvider
import com.kieronquinn.app.taptap.utils.wakefulnessLifecycle

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
enum class TapGate(val clazz: Class<*>, val category: TapGateCategory, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, @StringRes val whenDescriptionRes: Int, @StringRes val formattableDescription: Int? = null, val dataType: GateDataTypes? = null) {
    POWER_STATE(PowerState::class.java, TapGateCategory.DEVICE, R.string.gate_power_state, R.string.gate_power_state_desc, R.drawable.ic_power_state, R.string.gate_power_state_desc_when),
    POWER_STATE_INVERSE(PowerStateInverse::class.java, TapGateCategory.DEVICE, R.string.gate_power_state_inverse, R.string.gate_power_state_inverse_desc, R.drawable.ic_gate_power_state_inverse, R.string.gate_power_state_inverse_desc_when),
    LOCK_SCREEN(LockScreenState::class.java, TapGateCategory.DEVICE, R.string.gate_lock_screen_showing, R.string.gate_lock_screen_showing_desc, R.drawable.ic_gate_locked, R.string.gate_lock_screen_showing_desc_when),
    LOCK_SCREEN_INVERSE(LockScreenState::class.java, TapGateCategory.DEVICE, R.string.gate_lock_screen_showing_inverse, R.string.gate_lock_screen_showing_inverse_desc, R.drawable.ic_gate_unlocked, R.string.gate_lock_screen_showing_inverse_desc_when),
    CHARGING_STATE(ChargingState::class.java, TapGateCategory.DEVICE, R.string.gate_charging_state, R.string.gate_charging_state_desc, R.drawable.ic_charging_state, R.string.gate_charging_state_desc_when),
    USB_STATE(UsbState::class.java, TapGateCategory.DEVICE, R.string.gate_usb_state, R.string.gate_usb_state_desc, R.drawable.ic_usb_state, R.string.gate_usb_state_desc_when),
    CAMERA_VISIBILITY(CameraVisibility::class.java, TapGateCategory.EVENTS, R.string.gate_camera_visibility, R.string.gate_camera_visibility_desc, R.drawable.ic_camera_visibility, R.string.gate_camera_visibility_desc_when),
    TELEPHONY_ACTIVITY(TelephonyActivity::class.java, TapGateCategory.AUDIO, R.string.gate_telephony_activity, R.string.gate_telephony_activity_desc, R.drawable.ic_telephony_activity, R.string.gate_telephony_activity_desc_when),
    APP_SHOWING(AppVisibility::class.java, TapGateCategory.EVENTS, R.string.gate_app_showing, R.string.gate_app_showing_desc, R.drawable.ic_app_showing, R.string.gate_app_showing_desc_when, R.string.gate_app_showing_desc_formatted, GateDataTypes.PACKAGE_NAME),
    KEYBOARD_VISIBILITY(KeyboardVisibility::class.java, TapGateCategory.EVENTS, R.string.gate_keyboard_visibility, R.string.gate_keyboard_visibility_desc, R.drawable.ic_gate_keyboard_visibility, R.string.gate_keyboard_visibility_desc_when),
    ORIENTATION_LANDSCAPE(Orientation::class.java, TapGateCategory.SENSORS, R.string.gate_orientation_landscape, R.string.gate_orientation_landscape_desc, R.drawable.ic_gate_orientation, R.string.gate_orientation_landscape_desc_when),
    ORIENTATION_PORTRAIT(Orientation::class.java, TapGateCategory.SENSORS, R.string.gate_orientation_portrait, R.string.gate_orientation_portrait_desc, R.drawable.ic_gate_orientation, R.string.gate_orientation_portrait_desc_when),
    TABLE(TableDetection::class.java, TapGateCategory.SENSORS, R.string.gate_table, R.string.gate_table_desc, R.drawable.ic_gate_table, R.string.gate_table_desc_when),
    POCKET(PocketDetection::class.java, TapGateCategory.SENSORS, R.string.gate_pocket, R.string.gate_pocket_desc, R.drawable.ic_gate_pocket, R.string.gate_pocket_desc_when),
    HEADSET(Headset::class.java, TapGateCategory.AUDIO, R.string.gate_headset, R.string.gate_headset_desc, R.drawable.ic_gate_headset_inverse, R.string.gate_headset_desc_when),
    HEADSET_INVERSE(HeadsetInverse::class.java, TapGateCategory.AUDIO, R.string.gate_headset_inverse, R.string.gate_headset_desc_inverse, R.drawable.ic_gate_headset, R.string.gate_headset_desc_when_inverse),
    MUSIC(Music::class.java, TapGateCategory.AUDIO, R.string.gate_music, R.string.gate_music_desc, R.drawable.ic_gate_music, R.string.gate_music_desc_when),
    MUSIC_INVERSE(MusicInverse::class.java, TapGateCategory.AUDIO, R.string.gate_music_inverse, R.string.gate_music_desc_inverse, R.drawable.ic_gate_music_inverse, R.string.gate_music_desc_when_inverse),
    ALARM(Alarm::class.java, TapGateCategory.EVENTS, R.string.gate_alarm, R.string.gate_alarm_desc, R.drawable.ic_gate_alarm, R.string.gate_alarm_desc_when),
    FOLDABLE_CLOSED(FoldableClosed::class.java, TapGateCategory.DEVICE, R.string.gate_foldable_closed, R.string.gate_foldable_closed_desc, R.drawable.ic_gate_foldable_closed, R.string.gate_foldable_closed_desc_when),
    FOLDABLE_OPEN(FoldableOpen::class.java, TapGateCategory.DEVICE, R.string.gate_foldable_open, R.string.gate_foldable_open_desc, R.drawable.ic_gate_foldable_open, R.string.gate_foldable_open_desc_when)
}

val DEFAULT_GATES = arrayOf(TapGate.POWER_STATE, TapGate.TELEPHONY_ACTIVITY, TapGate.KEYBOARD_VISIBILITY)
val ALL_NON_CONFIG_GATES = arrayOf(TapGate.POWER_STATE, TapGate.POWER_STATE_INVERSE, TapGate.USB_STATE, TapGate.TELEPHONY_ACTIVITY, TapGate.CHARGING_STATE, TapGate.KEYBOARD_VISIBILITY, TapGate.POCKET, TapGate.TABLE)

enum class GateDataTypes {
    PACKAGE_NAME
}

fun TapGate.isGateDataSatisfied(context: Context): Boolean {
    return when(dataType){
        GateDataTypes.PACKAGE_NAME -> false
        else -> true
    }
}

fun TapGate.isSupported(context: Context): Boolean {
    return when(this){
        TapGate.FOLDABLE_CLOSED, TapGate.FOLDABLE_OPEN -> SidecarProvider.isDeviceFoldable(context)
        else -> true
    }
}

fun getGate(context: Context, tapGate: TapGate?, data: String?): Gate? {
    tapGate ?: return null
    return when (tapGate) {
        TapGate.POWER_STATE -> PowerState(context, wakefulnessLifecycle)
        TapGate.POWER_STATE_INVERSE -> PowerStateInverse(context)
        TapGate.LOCK_SCREEN -> LockScreenState(context)
        TapGate.LOCK_SCREEN_INVERSE -> LockScreenStateInverse(context)
        TapGate.CHARGING_STATE -> ChargingState(context, Handler(), 500L)
        TapGate.TELEPHONY_ACTIVITY -> TelephonyActivity(context)
        TapGate.CAMERA_VISIBILITY -> CameraVisibility(context)
        TapGate.USB_STATE -> UsbState(context, Handler(), 500L)
        TapGate.APP_SHOWING -> AppVisibility(context, data!!)
        TapGate.KEYBOARD_VISIBILITY -> KeyboardVisibility(context)
        TapGate.ORIENTATION_LANDSCAPE -> Orientation(context, Configuration.ORIENTATION_LANDSCAPE)
        TapGate.ORIENTATION_PORTRAIT -> Orientation(context, Configuration.ORIENTATION_PORTRAIT)
        TapGate.TABLE -> TableDetection(context)
        TapGate.POCKET -> PocketDetection(context)
        TapGate.HEADSET -> Headset(context)
        TapGate.HEADSET_INVERSE -> HeadsetInverse(context)
        TapGate.MUSIC -> Music(context)
        TapGate.MUSIC_INVERSE -> MusicInverse(context)
        TapGate.ALARM -> Alarm(context)
        TapGate.FOLDABLE_CLOSED -> FoldableClosed(context)
        TapGate.FOLDABLE_OPEN -> FoldableOpen(context)
    }
}