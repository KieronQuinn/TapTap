package com.kieronquinn.app.taptap.models.gate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.gates.custom.AlarmGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.AppVisibilityGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.BatterySaverGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.CameraVisibilityGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.ChargingStateGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.FoldableClosedGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.FoldableOpenGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.HeadsetGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.HeadsetInverseGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.KeyboardVisibilityGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.LockScreenStateGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.LowBatteryGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.MusicGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.MusicInverseGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.OrientationGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.PocketDetectionGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.PowerStateGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.PowerStateInverseGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.TableDetectionGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.TelephonyActivityGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.UsbStateGate
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory

/**
 *  [TapTapActionDirectory] is an emum of all Gates.
 *
 *  **Arguments:**
 *  - [clazz]: The [Class] of the actual Gate. Not currently used, but useful for linking.
 *  - [category]: The [TapTapGateCategory] of the gate
 *  - [nameRes]: String resource ID of the gate's name
 *  - [descriptionRes]: String resource ID of the gate's description, unformatted
 *  - [iconRes]: Drawable resource ID of the gate's icon
 *  - [whenDescriptionRes]: String resource ID of the gate's "when gate" description, unformatted
 *  - [formattableDescription]: String resource ID of the gate's formattable description, which can be used to insert data later
 *  - [dataType]: The type of data stored in the gate's data string in the database
 *  - [gateSupportedRequirement]: A single requirement for the gate to be supported, or `null` if it's always supported
 *  - [gateRequirement]: An array of requirements for the gate to be added, eg. permissions. `null` if there are no requirements.
 */
enum class TapTapGateDirectory(
    val clazz: Class<*>,
    val category: TapTapGateCategory,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val whenDescriptionRes: Int,
    @StringRes val formattableDescription: Int? = null,
    @StringRes val formattableWhenDescription: Int? = null,
    val dataType: GateDataTypes? = null,
    val gateSupportedRequirement: GateSupportedRequirement? = null,
    val gateRequirement: Array<GateRequirement>? = null
) {
    POWER_STATE(
        PowerStateGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_power_state,
        R.string.gate_power_state_desc,
        R.drawable.ic_gate_power_state,
        R.string.gate_power_state_desc_when
    ),
    POWER_STATE_INVERSE(
        PowerStateInverseGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_power_state_inverse,
        R.string.gate_power_state_inverse_desc,
        R.drawable.ic_gate_power_state_inverse,
        R.string.gate_power_state_inverse_desc_when
    ),
    LOCK_SCREEN(
        LockScreenStateGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_lock_screen_showing,
        R.string.gate_lock_screen_showing_desc,
        R.drawable.ic_gate_locked,
        R.string.gate_lock_screen_showing_desc_when
    ),
    LOCK_SCREEN_INVERSE(
        LockScreenStateGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_lock_screen_showing_inverse,
        R.string.gate_lock_screen_showing_inverse_desc,
        R.drawable.ic_gate_unlocked,
        R.string.gate_lock_screen_showing_inverse_desc_when
    ),
    CHARGING_STATE(
        ChargingStateGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_charging_state,
        R.string.gate_charging_state_desc_a,
        R.drawable.ic_gate_charging_state,
        R.string.gate_charging_state_desc_when_a
    ),
    USB_STATE(
        UsbStateGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_usb_state,
        R.string.gate_usb_state_desc_a,
        R.drawable.ic_gate_usb_state,
        R.string.gate_usb_state_desc_when_a
    ),
    CAMERA_VISIBILITY(
        CameraVisibilityGate::class.java,
        TapTapGateCategory.EVENTS,
        R.string.gate_camera_visibility,
        R.string.gate_camera_visibility_desc,
        R.drawable.ic_gate_camera_visibility,
        R.string.gate_camera_visibility_desc_when,
        gateRequirement = arrayOf(GateRequirement.Accessibility)
    ),
    TELEPHONY_ACTIVITY(
        TelephonyActivityGate::class.java,
        TapTapGateCategory.AUDIO,
        R.string.gate_telephony_activity,
        R.string.gate_telephony_activity_desc,
        R.drawable.ic_gate_telephony_activity,
        R.string.gate_telephony_activity_desc_when,
        gateRequirement = arrayOf(GateRequirement.ReadPhoneStatePermission)
    ),
    APP_SHOWING(
        AppVisibilityGate::class.java,
        TapTapGateCategory.EVENTS,
        R.string.gate_app_showing,
        R.string.gate_app_showing_desc,
        R.drawable.ic_gate_app_visibility,
        R.string.gate_app_showing_desc_when,
        R.string.gate_app_showing_desc_formatted,
        R.string.gate_app_showing_desc_when_formatted,
        dataType = GateDataTypes.PACKAGE_NAME,
        gateRequirement = arrayOf(GateRequirement.Accessibility)
    ),
    KEYBOARD_VISIBILITY(
        KeyboardVisibilityGate::class.java,
        TapTapGateCategory.EVENTS,
        R.string.gate_keyboard_visibility,
        R.string.gate_keyboard_visibility_desc,
        R.drawable.ic_gate_keyboard_visibility,
        R.string.gate_keyboard_visibility_desc_when,
        gateRequirement = arrayOf(GateRequirement.Accessibility)
    ),
    ORIENTATION_LANDSCAPE(
        OrientationGate::class.java,
        TapTapGateCategory.SENSORS,
        R.string.gate_orientation_landscape,
        R.string.gate_orientation_landscape_desc,
        R.drawable.ic_gate_orientation,
        R.string.gate_orientation_landscape_desc_when
    ),
    ORIENTATION_PORTRAIT(
        OrientationGate::class.java,
        TapTapGateCategory.SENSORS,
        R.string.gate_orientation_portrait,
        R.string.gate_orientation_portrait_desc,
        R.drawable.ic_gate_orientation,
        R.string.gate_orientation_portrait_desc_when
    ),
    TABLE(
        TableDetectionGate::class.java,
        TapTapGateCategory.SENSORS,
        R.string.gate_table,
        R.string.gate_table_desc,
        R.drawable.ic_gate_table,
        R.string.gate_table_desc_when
    ),
    POCKET(
        PocketDetectionGate::class.java,
        TapTapGateCategory.SENSORS,
        R.string.gate_pocket,
        R.string.gate_pocket_desc,
        R.drawable.ic_gate_pocket,
        R.string.gate_pocket_desc_when
    ),
    HEADSET(
        HeadsetGate::class.java,
        TapTapGateCategory.AUDIO,
        R.string.gate_headset,
        R.string.gate_headset_desc,
        R.drawable.ic_gate_headset_inverse,
        R.string.gate_headset_desc_when
    ),
    HEADSET_INVERSE(
        HeadsetInverseGate::class.java,
        TapTapGateCategory.AUDIO,
        R.string.gate_headset_inverse,
        R.string.gate_headset_desc_inverse,
        R.drawable.ic_gate_headset,
        R.string.gate_headset_desc_when_inverse
    ),
    MUSIC(
        MusicGate::class.java,
        TapTapGateCategory.AUDIO,
        R.string.gate_music,
        R.string.gate_music_desc,
        R.drawable.ic_gate_music,
        R.string.gate_music_desc_when
    ),
    MUSIC_INVERSE(
        MusicInverseGate::class.java,
        TapTapGateCategory.AUDIO,
        R.string.gate_music_inverse,
        R.string.gate_music_desc_inverse,
        R.drawable.ic_gate_music_inverse,
        R.string.gate_music_desc_when_inverse
    ),
    ALARM(
        AlarmGate::class.java,
        TapTapGateCategory.EVENTS,
        R.string.gate_alarm,
        R.string.gate_alarm_desc,
        R.drawable.ic_gate_alarm,
        R.string.gate_alarm_desc_when
    ),
    FOLDABLE_CLOSED(
        FoldableClosedGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_foldable_closed,
        R.string.gate_foldable_closed_desc,
        R.drawable.ic_gate_foldable_closed,
        R.string.gate_foldable_closed_desc_when,
        gateSupportedRequirement = GateSupportedRequirement.Foldable
    ),
    FOLDABLE_OPEN(
        FoldableOpenGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_foldable_open,
        R.string.gate_foldable_open_desc,
        R.drawable.ic_gate_foldable_open,
        R.string.gate_foldable_open_desc_when,
        gateSupportedRequirement = GateSupportedRequirement.Foldable
    ),
    LOW_BATTERY(
        LowBatteryGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_low_battery,
        R.string.gate_low_battery_desc,
        R.drawable.ic_gate_low_battery,
        R.string.gate_low_battery_desc_when
    ),
    BATTERY_SAVER(
        BatterySaverGate::class.java,
        TapTapGateCategory.DEVICE,
        R.string.gate_battery_saver,
        R.string.gate_battery_saver_desc,
        R.drawable.ic_gate_battery_saver,
        R.string.gate_battery_saver_desc_when
    );

    companion object {
        fun valueFor(name: String): TapTapGateDirectory? {
            return values().firstOrNull { it.name == name }
        }
    }
}

enum class GateDataTypes {
    PACKAGE_NAME
}

sealed class GateSupportedRequirement(@StringRes val description: Int) {
    data class MinSdk(val version: Int): GateSupportedRequirement(R.string.gate_unsupported_reason_min_sdk)
    object Foldable: GateSupportedRequirement(R.string.gate_unsupported_reason_foldable)
}

sealed class GateRequirement {

    abstract class Permission: GateRequirement()
    abstract class UserDisplayedGateRequirement(@DrawableRes val icon: Int, @StringRes val label: Int, @StringRes val desc: Int): GateRequirement()

    object ReadPhoneStatePermission: Permission()

    object Shizuku : UserDisplayedGateRequirement(
        R.drawable.ic_shizuku,
        R.string.action_chip_shizuku,
        R.string.action_chip_shizuku_desc
    )

    object Accessibility : UserDisplayedGateRequirement(
        R.drawable.ic_action_chip_accessibility,
        R.string.action_chip_accessibility,
        R.string.action_chip_accessibility_desc
    )

    //Currently unused
    object Root : UserDisplayedGateRequirement(
        R.drawable.ic_action_chip_root,
        R.string.action_chip_root,
        R.string.action_chip_root_desc
    )

}