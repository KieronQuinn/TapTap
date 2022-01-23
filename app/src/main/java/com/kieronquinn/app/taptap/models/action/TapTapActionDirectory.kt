package com.kieronquinn.app.taptap.models.action

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.actions.custom.*

/**
 *  [TapTapActionDirectory] is an emum of all Actions.
 *
 *  **Arguments:**
 *  - [clazz]: The [Class] of the actual Action. Not currently used, but useful for linking.
 *  - [category]: The [TapTapActionCategory] of the action
 *  - [nameRes]: String resource ID of the action's name
 *  - [descriptionRes]: String resource ID of the action's description, unformatted
 *  - [iconRes]: Drawable resource ID of the action's icon
 *  - [actionSupportedRequirement]: A single requirement for the action to be supported, or `null` if it's always supported
 *  - [formattableDescription]: String resource ID of the action's formattable description, which can be used to insert data later
 *  - [dataType]: The type of data stored in the action's data string in the database
 *  - [actionRequirement]: An array of requirements for the action to be added, eg. permissions. `null` if there are no requirements.
 */
enum class TapTapActionDirectory(
    val clazz: Class<*>,
    val category: TapTapActionCategory,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val actionSupportedRequirement: ActionSupportedRequirement?,
    @StringRes val formattableDescription: Int? = null,
    val dataType: ActionDataTypes? = null,
    val actionRequirement: Array<ActionRequirement>? = null
) {
    LAUNCH_APP(
        LaunchAppAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_app,
        R.string.action_launch_app_desc,
        R.drawable.ic_action_launch_app,
        null,
        R.string.action_launch_app_desc_formattable,
        ActionDataTypes.PACKAGE_NAME,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    LAUNCH_SHORTCUT(
        LaunchShortcutAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_shortcut,
        R.string.action_launch_shortcut_desc,
        R.drawable.ic_action_launch_shortcut,
        null,
        R.string.action_launch_shortcut_desc_formattable,
        ActionDataTypes.SHORTCUT,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    LAUNCH_APP_SHORTCUT(
        LaunchAppShortcutAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_app_shortcut,
        R.string.action_launch_app_shortcut_desc,
        R.drawable.ic_action_launch_app_shortcut,
        null,
        R.string.action_launch_app_shortcut_desc_formattable,
        ActionDataTypes.APP_SHORTCUT,
        actionRequirement = arrayOf(ActionRequirement.Shizuku)
    ),
    LAUNCH_ASSISTANT(
        LaunchAssistantAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_assistant,
        R.string.action_launch_assistant_desc,
        R.drawable.ic_action_launch_assistant,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    LAUNCH_SEARCH(
        LaunchSearchAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_search,
        R.string.action_launch_search_desc,
        R.drawable.ic_action_launch_search,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    LAUNCH_CAMERA(
        LaunchCameraAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_launch_camera,
        R.string.action_launch_camera_desc,
        R.drawable.ic_action_launch_camera,
        null,
        actionRequirement = arrayOf(ActionRequirement.CameraPermission, ActionRequirement.DrawOverOtherAppsPermission)
    ),
    SNAPCHAT(
        SnapchatAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_snapchat,
        R.string.action_snapchat_desc,
        R.drawable.ic_action_snapchat,
        ActionSupportedRequirement.Snapchat,
        actionRequirement = arrayOf(ActionRequirement.Snapchat)
    ),
    SCREENSHOT(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.UTILITIES,
        R.string.action_screenshot,
        R.string.action_screenshot_desc,
        R.drawable.ic_action_screenshot,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.P),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    NOTIFICATIONS(
        NotificationsExpandAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_notifications,
        R.string.action_notifications_desc,
        R.drawable.ic_action_notifications,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    QUICK_SETTINGS(
        QuickSettingsExpandAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_quick_settings,
        R.string.action_quick_settings_desc,
        R.drawable.ic_action_quick_settings,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    LOCK_SCREEN(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_lock_screen,
        R.string.action_lock_screen_desc,
        R.drawable.ic_action_lock_screen,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.P),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    WAKE_DEVICE(
        WakeDeviceAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_wake_device,
        R.string.action_wake_device_desc,
        R.drawable.ic_action_wake_device,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    HOME(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_home,
        R.string.action_home_desc,
        R.drawable.ic_action_home,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    BACK(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_back,
        R.string.action_back_desc,
        R.drawable.ic_action_back,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    RECENTS(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_recents,
        R.string.action_recents_desc,
        R.drawable.ic_action_recent,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    SPLIT_SCREEN(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.UTILITIES,
        R.string.action_split_screen,
        R.string.action_split_screen_desc,
        R.drawable.ic_action_split_screen,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    REACHABILITY(
        LaunchReachabilityAction::class.java,
        TapTapActionCategory.UTILITIES,
        R.string.action_reachability,
        R.string.action_reachability_desc,
        R.drawable.ic_action_reachability,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility, ActionRequirement.DrawOverOtherAppsPermission)
    ),
    POWER_DIALOG(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_power_dialog,
        R.string.action_power_dialog_desc,
        R.drawable.ic_action_power_dialog,
        null,
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    APP_DRAWER(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.UTILITIES,
        R.string.action_app_drawer,
        R.string.action_app_drawer_desc,
        R.drawable.ic_action_app_drawer,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.R),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    ALT_TAB(
        AltTabAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_alt_tab,
        R.string.action_alt_tab_desc,
        R.drawable.ic_action_alt_tab,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    FLASHLIGHT(
        FlashlightAction::class.java,
        TapTapActionCategory.UTILITIES,
        R.string.action_flashlight,
        R.string.action_flashlight_desc,
        R.drawable.ic_action_flashlight,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission, ActionRequirement.CameraPermission)
    ),
    TASKER_EVENT(
        TaskerEventAction::class.java,
        TapTapActionCategory.ADVANCED,
        R.string.action_tasker_event,
        R.string.action_tasker_event_desc,
        R.drawable.ic_action_tasker,
        ActionSupportedRequirement.Tasker,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission, ActionRequirement.Tasker, ActionRequirement.TaskerPermission)
    ),
    TASKER_TASK(
        TaskerTaskAction::class.java,
        TapTapActionCategory.ADVANCED,
        R.string.action_tasker_task,
        R.string.action_tasker_task_desc,
        R.drawable.ic_action_tasker,
        ActionSupportedRequirement.Tasker,
        R.string.action_tasker_task_desc_formatted,
        dataType = ActionDataTypes.TASKER_TASK,
        actionRequirement = arrayOf(ActionRequirement.Tasker, ActionRequirement.TaskerPermission)
    ),
    TOGGLE_PAUSE(
        MusicAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_toggle_pause,
        R.string.action_toggle_pause_desc,
        R.drawable.ic_action_toggle_pause,
        null
    ),
    PREVIOUS(
        MusicAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_previous,
        R.string.action_previous_desc,
        R.drawable.ic_action_previous,
        null
    ),
    NEXT(
        MusicAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_next,
        R.string.action_next_desc,
        R.drawable.ic_action_next,
        null
    ),
    SOUND_PROFILE(
        SoundProfileAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.actions_sound_profile,
        R.string.actions_sound_profile_desc,
        R.drawable.ic_action_sound_profile,
        null,
        actionRequirement = arrayOf(ActionRequirement.AccessNotificationPolicyPermission)
    ),
    VOLUME_PANEL(
        VolumeAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_volume_panel,
        R.string.action_volume_panel_desc,
        R.drawable.ic_action_volume_panel,
        null
    ),
    VOLUME_UP(
        VolumeAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_volume_up,
        R.string.action_volume_up_desc,
        R.drawable.ic_action_volume_up,
        null
    ),
    VOLUME_DOWN(
        VolumeAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_volume_down,
        R.string.action_volume_down_desc,
        R.drawable.ic_action_volume_down,
        null
    ),
    VOLUME_TOGGLE_MUTE(
        VolumeAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_volume_toggle_mute,
        R.string.action_volume_toggle_mute_desc,
        R.drawable.ic_action_volume_toggle_mute,
        null
    ),
    ALARM_TIMER(
        AlarmTimerAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_alarm_timer,
        R.string.action_alarm_timer_desc,
        R.drawable.ic_action_alarm_timer,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    ALARM_SNOOZE(
        AlarmSnoozeAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_alarm_snooze,
        R.string.action_alarm_snooze_desc,
        R.drawable.ic_action_alarm_snooze,
        null,
        actionRequirement = arrayOf(ActionRequirement.DrawOverOtherAppsPermission)
    ),
    GOOGLE_VOICE_ACCESS(
        GoogleVoiceAccessAction::class.java,
        TapTapActionCategory.ACCESSIBILITY,
        R.string.action_google_voice_access,
        R.string.action_google_voice_access_desc,
        R.drawable.ic_action_google_voice_access,
        null
    ),
    ACCESSIBILITY_BUTTON(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.ACCESSIBILITY,
        R.string.action_accessibility_button,
        R.string.action_accessibility_button_desc,
        R.drawable.ic_action_accessibility,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.R),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    ACCESSIBILITY_BUTTON_CHOOSER(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.ACCESSIBILITY,
        R.string.action_accessibility_button_chooser,
        R.string.action_accessibility_button_chooser_desc,
        R.drawable.ic_action_accessibility,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.R),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    ACCESSIBILITY_SHORTCUT(
        AccessibilityServiceGlobalAction::class.java,
        TapTapActionCategory.ACCESSIBILITY,
        R.string.action_accessibility_shortcut,
        R.string.action_accessibility_shortcut_desc,
        R.drawable.ic_action_accessibility,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.R),
        actionRequirement = arrayOf(ActionRequirement.Accessibility)
    ),
    HAMBURGER(
        HamburgerAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_hamburger,
        R.string.action_hamburger_desc,
        R.drawable.ic_action_hamburger,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.GestureAccessibility)
    ),
    ACCEPT_CALL(
        AcceptCallAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_accept_call,
        R.string.action_accept_call_desc,
        R.drawable.ic_action_accept_call,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.O),
        actionRequirement = arrayOf(ActionRequirement.AnswerPhoneCallsPermission)
    ),
    REJECT_CALL(
        RejectCallAction::class.java,
        TapTapActionCategory.SOUND,
        R.string.action_reject_call,
        R.string.action_reject_call_desc,
        R.drawable.ic_action_reject_call,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.P),
        actionRequirement = arrayOf(ActionRequirement.AnswerPhoneCallsPermission)
    ),
    SWIPE_UP(
        SwipeAction::class.java,
        TapTapActionCategory.GESTURE,
        R.string.action_swipe_up,
        R.string.action_swipe_up_desc,
        R.drawable.ic_action_swipe_up,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.GestureAccessibility)
    ),
    SWIPE_DOWN(
        SwipeAction::class.java,
        TapTapActionCategory.GESTURE,
        R.string.action_swipe_down,
        R.string.action_swipe_down_desc,
        R.drawable.ic_action_swipe_down,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.GestureAccessibility)
    ),
    SWIPE_LEFT(
        SwipeAction::class.java,
        TapTapActionCategory.GESTURE,
        R.string.action_swipe_left,
        R.string.action_swipe_left_desc,
        R.drawable.ic_action_swipe_left,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.GestureAccessibility)
    ),
    SWIPE_RIGHT(
        SwipeAction::class.java,
        TapTapActionCategory.GESTURE,
        R.string.action_swipe_right,
        R.string.action_swipe_right_desc,
        R.drawable.ic_action_swipe_right,
        ActionSupportedRequirement.MinSdk(Build.VERSION_CODES.N),
        actionRequirement = arrayOf(ActionRequirement.GestureAccessibility)
    ),
    CAMERA_SHUTTER(
        CameraShutterAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_camera_shutter,
        R.string.action_camera_shutter_desc,
        R.drawable.ic_action_camera_shutter,
        null,
        actionRequirement = arrayOf(ActionRequirement.Shizuku)
    ),
    DEVICE_CONTROLS(
        LaunchDeviceControlsAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_device_controls,
        R.string.action_device_controls_desc,
        R.drawable.ic_action_device_controls,
        ActionSupportedRequirement.Intent(LaunchDeviceControlsAction.DEVICE_CONTROLS_INTENT),
        actionRequirement = arrayOf(ActionRequirement.Root)
    ),
    QUICK_ACCESS_WALLET(
        LaunchQuickAccessWalletAction::class.java,
        TapTapActionCategory.LAUNCH,
        R.string.action_quick_access_wallet,
        R.string.action_quick_access_wallet_desc,
        R.drawable.ic_action_quick_access_wallet,
        ActionSupportedRequirement.Intent(LaunchQuickAccessWalletAction.QUICK_ACCESS_WALLET_INTENT),
        actionRequirement = arrayOf(ActionRequirement.Root)
    ),
    QUICK_SETTING(
        LaunchQuickAccessWalletAction::class.java,
        TapTapActionCategory.BUTTON,
        R.string.action_click_quick_setting,
        R.string.action_click_quick_setting_desc,
        R.drawable.ic_action_quick_settings,
        formattableDescription = R.string.action_click_quick_setting_desc_formatted,
        dataType = ActionDataTypes.QUICK_SETTING,
        actionSupportedRequirement = null,
        actionRequirement = arrayOf(ActionRequirement.Shizuku)
    );

    companion object {
        fun valueFor(name: String): TapTapActionDirectory? {
            return values().firstOrNull { it.name == name }
        }
    }
}

enum class ActionDataTypes {
    PACKAGE_NAME,
    TASKER_TASK,
    SHORTCUT,
    APP_SHORTCUT,
    QUICK_SETTING
}

sealed class ActionSupportedRequirement {
    data class MinSdk(val version: Int): ActionSupportedRequirement()
    data class Intent(val intent: android.content.Intent): ActionSupportedRequirement()
    object Tasker: ActionSupportedRequirement()
    object Snapchat: ActionSupportedRequirement()
}

sealed class ActionRequirement {

    abstract class Permission: ActionRequirement()
    abstract class UserDisplayedActionRequirement(@DrawableRes val icon: Int, @StringRes val label: Int, @StringRes val desc: Int): ActionRequirement()

    object DrawOverOtherAppsPermission: Permission()
    object CameraPermission: Permission()
    object AnswerPhoneCallsPermission: Permission()
    object AccessNotificationPolicyPermission: Permission()
    object TaskerPermission: Permission()

    object Shizuku : UserDisplayedActionRequirement(
        R.drawable.ic_shizuku,
        R.string.action_chip_shizuku,
        R.string.action_chip_shizuku_desc
    )

    object Accessibility : UserDisplayedActionRequirement(
        R.drawable.ic_action_chip_accessibility,
        R.string.action_chip_accessibility,
        R.string.action_chip_accessibility_desc
    )

    object GestureAccessibility : UserDisplayedActionRequirement(
        R.drawable.ic_action_chip_accessibility_gesture,
        R.string.action_chip_gesture_accessibility,
        R.string.action_chip_gesture_accessibility_desc
    )

    object Tasker : UserDisplayedActionRequirement(
        R.drawable.ic_action_chip_tasker,
        R.string.action_chip_tasker,
        R.string.action_chip_tasker_desc
    )

    object Snapchat : UserDisplayedActionRequirement(
        R.drawable.ic_action_snapchat,
        R.string.action_chip_snapchat,
        R.string.action_chip_snapchat_desc
    )

    object Root : UserDisplayedActionRequirement(
        R.drawable.ic_action_chip_root,
        R.string.action_chip_root,
        R.string.action_chip_root_desc
    )

}

