package com.kieronquinn.app.taptap.models

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.columbus.actions.*
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.doesHavePermissions
import com.kieronquinn.app.taptap.utils.extensions.isAccessibilityServiceEnabled
import com.kieronquinn.app.taptap.utils.extensions.isTaskerInstalled

/*
    Tap Actions (wrapping the Action class) contain details on how the action will be shown in the settings
    clazz: The class name of the actual Action that will be run. This is not currently used, but may be used for verification later down the line
    category: The category the action will be shown in in the picker in the app. See TapActionCategory for a list
    nameRes: The String resource to use for the name/title of the action
    descriptionRes: The String resource to use for the description of the action
    iconRes: The Drawable resource to use for the icon of the action
    isAvailable: Whether the action is available on the device. For example, you should the static method minSdk(<version>) if it's only compatible with a certain version of Android and above
    isWhenAvailable: Whether the action should be allowed to have "when gates" (gates which are required to run the action). Currently this is true for all the gates, but it's there for future-proofing
    canBlock: *Important*: Whether the action's isAvailable method always returns true. If this is set to true, the UI will warn the user that no actions below it in the list will ever be run, as this will be the last one that can be
    formattableDescription (optional) : The String resource for a description of this action that can be formatted to contain the action's data, such as an app name for the Launch App action
    dataType (optional): The kind of data that is stored in the 'data' field of ActionInternal for this action. This will be used to open pickers in the UI after an action is added, for example an app picker. See ActionDataTypes for a full list
 */
enum class TapAction(val clazz: Class<*>, val category: TapActionCategory, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, val isAvailable: Boolean, val isWhenAvailable: Boolean, val canBlock: Boolean, @StringRes val formattableDescription: Int? = null, val dataType: ActionDataTypes? = null) {
    LAUNCH_APP(LaunchApp::class.java, TapActionCategory.LAUNCH, R.string.action_launch_app, R.string.action_launch_app_desc, R.drawable.ic_action_category_launch, true, true, false, R.string.action_launch_app_desc_formattable, ActionDataTypes.PACKAGE_NAME),
    LAUNCH_SHORTCUT(LaunchShortcut::class.java, TapActionCategory.LAUNCH, R.string.action_launch_shortcut, R.string.action_launch_shortcut_desc, R.drawable.ic_action_shortcut, true, true, false, R.string.action_launch_shortcut_desc_formattable, ActionDataTypes.SHORTCUT),
    LAUNCH_ASSISTANT(LaunchAssistant::class.java, TapActionCategory.LAUNCH, R.string.action_launch_assistant, R.string.action_launch_assistant_desc, R.drawable.ic_launch_assistant, true, true, false),
    LAUNCH_SEARCH(LaunchSearch::class.java, TapActionCategory.LAUNCH, R.string.action_launch_search, R.string.action_launch_search_desc, R.drawable.ic_search, true, true, false),
    LAUNCH_CAMERA(LaunchCamera::class.java, TapActionCategory.LAUNCH, R.string.action_launch_camera, R.string.action_launch_camera_desc, R.drawable.ic_camera_visibility, true, true, false, dataType = ActionDataTypes.CAMERA_PERMISSION),
    SCREENSHOT(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_screenshot, R.string.action_screenshot_desc, R.drawable.ic_action_screenshot, minSdk(Build.VERSION_CODES.P), true, true),
    NOTIFICATIONS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_notifications, R.string.action_notifications_desc, R.drawable.ic_action_notifications, true, true, true),
    QUICK_SETTINGS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_quick_settings, R.string.action_quick_settings_desc, R.drawable.ic_action_quick_settings, true, true, true),
    LOCK_SCREEN(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_lock_screen, R.string.action_lock_screen_desc, R.drawable.ic_power_state, minSdk(Build.VERSION_CODES.P), true, true),
    WAKE_DEVICE(WakeDeviceAction::class.java, TapActionCategory.ACTIONS, R.string.action_wake_device, R.string.action_wake_device_desc, R.drawable.ic_wake_from_sleep, true, true, false),
    HOME(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_home, R.string.action_home_desc, R.drawable.ic_action_home, true, true, true),
    BACK(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_back, R.string.action_back_desc, R.drawable.ic_action_back, true, true, true),
    RECENTS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_recents, R.string.action_recents_desc, R.drawable.ic_action_recent, true, true, true),
    SPLIT_SCREEN(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_split_screen, R.string.action_split_screen_desc, R.drawable.ic_action_split_screen, true, true, true),
    REACHABILITY(LaunchReachability::class.java, TapActionCategory.UTILITIES, R.string.action_reachability, R.string.action_reachability_desc, R.drawable.ic_action_reachability, true, true, true),
    POWER_DIALOG(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_power_dialog, R.string.action_power_dialog_desc, R.drawable.ic_action_power_dialog, true, true, true),
    APP_DRAWER(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_app_drawer, R.string.action_app_drawer_desc, R.drawable.ic_action_app_drawer, minSdk(Build.VERSION_CODES.R), true, true),
    ALT_TAB(AltTabAction::class.java, TapActionCategory.ACTIONS, R.string.action_alt_tab, R.string.action_alt_tab_desc, R.drawable.ic_action_alt_tab, minSdk(Build.VERSION_CODES.N), true, true),
    FLASHLIGHT(Flashlight::class.java, TapActionCategory.UTILITIES, R.string.action_flashlight, R.string.action_flashlight_desc, R.drawable.ic_action_category_utilities, true, true, true, dataType = ActionDataTypes.CAMERA_PERMISSION),
    TASKER_EVENT(TaskerEvent::class.java, TapActionCategory.ADVANCED, R.string.action_tasker_event, R.string.action_tasker_event_desc, R.drawable.ic_action_tasker, true, true, true),
    TASKER_TASK(TaskerTask::class.java, TapActionCategory.ADVANCED, R.string.action_tasker_task, R.string.action_tasker_task_desc, R.drawable.ic_action_tasker, true, true, true, R.string.action_tasker_task_desc_formatted, dataType = ActionDataTypes.TASKER_TASK),
    TOGGLE_PAUSE(MusicAction::class.java, TapActionCategory.ACTIONS, R.string.action_toggle_pause, R.string.action_toggle_pause_desc, R.drawable.ic_action_toggle_pause, true, true, true),
    PREVIOUS(MusicAction::class.java, TapActionCategory.ACTIONS, R.string.action_previous, R.string.action_previous_desc, R.drawable.ic_action_previous, true, true, true),
    NEXT(MusicAction::class.java, TapActionCategory.ACTIONS, R.string.action_next, R.string.action_next_desc, R.drawable.ic_action_next, true, true, true),
    SOUND_PROFILER(SoundProfileAction::class.java, TapActionCategory.ACTIONS, R.string.actions_sound_profile, R.string.actions_sound_profile_desc, R.drawable.ic_baseline_vibration_24, true, true, true, dataType = ActionDataTypes.ACCESS_NOTIFICATION_POLICY),
    VOLUME_PANEL(VolumeAction::class.java, TapActionCategory.ACTIONS, R.string.action_volume_panel, R.string.action_volume_panel_desc, R.drawable.ic_action_volume_panel, true, true, true),
    VOLUME_UP(VolumeAction::class.java, TapActionCategory.ACTIONS, R.string.action_volume_up, R.string.action_volume_up_desc, R.drawable.ic_action_volume_up, true, true, true),
    VOLUME_DOWN(VolumeAction::class.java, TapActionCategory.ACTIONS, R.string.action_volume_down, R.string.action_volume_down_desc, R.drawable.ic_action_volume_down, true, true, true),
    VOLUME_TOGGLE_MUTE(VolumeAction::class.java, TapActionCategory.ACTIONS, R.string.action_volume_toggle_mute, R.string.action_volume_toggle_mute_desc, R.drawable.ic_action_volume_toggle_mute, true, true, true),
    ALARM_TIMER(AlarmTimerAction::class.java, TapActionCategory.ACTIONS, R.string.action_alarm_timer, R.string.action_alarm_timer_desc, R.drawable.ic_gate_alarm, true, true, true),
    ALARM_SNOOZE(AlarmSnoozeAction::class.java, TapActionCategory.ACTIONS, R.string.action_alarm_snooze, R.string.action_alarm_snooze_desc, R.drawable.ic_action_alarm_snooze, true, true, true),
    GOOGLE_VOICE_ACCESS(GoogleVoiceAccessAction::class.java, TapActionCategory.ACTIONS, R.string.action_google_voice_access, R.string.action_google_voice_access_desc, R.drawable.ic_action_google_voice_access, true, true, true),
    ACCESSIBILITY_BUTTON(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_button, R.string.action_accessibility_button_desc, R.drawable.ic_action_accessibility, minSdk(Build.VERSION_CODES.R), true, true),
    ACCESSIBILITY_BUTTON_CHOOSER(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_button_chooser, R.string.action_accessibility_button_chooser_desc, R.drawable.ic_action_accessibility, minSdk(Build.VERSION_CODES.R), true, true),
    ACCESSIBILITY_SHORTCUT(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_shortcut, R.string.action_accessibility_shortcut_desc, R.drawable.ic_action_accessibility, minSdk(Build.VERSION_CODES.R), true, true),
    HAMBURGER(HamburgerAction::class.java, TapActionCategory.ADVANCED, R.string.action_hamburger, R.string.action_hamburger_desc, R.drawable.ic_action_hamburger, minSdk(Build.VERSION_CODES.N), true, true, dataType = ActionDataTypes.SECONDARY_GESTURE_SERVICE),
    ACCEPT_CALL(AcceptCall::class.java, TapActionCategory.ACTIONS, R.string.action_accept_call, R.string.action_accept_call_desc, R.drawable.ic_action_accept_call, minSdk(Build.VERSION_CODES.O), true, false, dataType = ActionDataTypes.ANSWER_PHONE_CALLS_PERMISSION),
    REJECT_CALL(RejectCall::class.java, TapActionCategory.ACTIONS, R.string.action_reject_call, R.string.action_reject_call_desc, R.drawable.ic_action_reject_call, minSdk(Build.VERSION_CODES.P), true, false, dataType = ActionDataTypes.ANSWER_PHONE_CALLS_PERMISSION),
    SWIPE_UP(SwipeAction::class.java, TapActionCategory.ADVANCED, R.string.action_swipe_up, R.string.action_swipe_up_desc, R.drawable.ic_action_swipe_up, minSdk(Build.VERSION_CODES.N), true, true, dataType = ActionDataTypes.SECONDARY_GESTURE_SERVICE),
    SWIPE_DOWN(SwipeAction::class.java, TapActionCategory.ADVANCED, R.string.action_swipe_down, R.string.action_swipe_down_desc, R.drawable.ic_action_swipe_down, minSdk(Build.VERSION_CODES.N), true, true, dataType = ActionDataTypes.SECONDARY_GESTURE_SERVICE),
    SWIPE_LEFT(SwipeAction::class.java, TapActionCategory.ADVANCED, R.string.action_swipe_left, R.string.action_swipe_left_desc, R.drawable.ic_action_swipe_left, minSdk(Build.VERSION_CODES.N), true, true, dataType = ActionDataTypes.SECONDARY_GESTURE_SERVICE),
    SWIPE_RIGHT(SwipeAction::class.java, TapActionCategory.ADVANCED, R.string.action_swipe_right, R.string.action_swipe_right_desc, R.drawable.ic_action_swipe_right, minSdk(Build.VERSION_CODES.N), true, true, dataType = ActionDataTypes.SECONDARY_GESTURE_SERVICE)
}

val GESTURE_REQUIRING_ACTIONS = TapAction.values().filter { it.dataType == ActionDataTypes.SECONDARY_GESTURE_SERVICE }

val DEFAULT_ACTIONS = if(TapAction.SCREENSHOT.isAvailable){
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.SCREENSHOT)
}else{
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.HOME)
}

val DEFAULT_ACTIONS_TRIPLE = arrayOf(TapAction.NOTIFICATIONS)

enum class ActionDataTypes {
    PACKAGE_NAME,
    CAMERA_PERMISSION,
    ANSWER_PHONE_CALLS_PERMISSION,
    TASKER_TASK,
    SHORTCUT,
    ACCESS_NOTIFICATION_POLICY,
    SECONDARY_GESTURE_SERVICE
}

fun TapAction.isActionDataSatisfied(context: Context): Boolean {
    return when(dataType){
        ActionDataTypes.PACKAGE_NAME -> false
        ActionDataTypes.CAMERA_PERMISSION -> context.doesHavePermissions(Manifest.permission.CAMERA)
        ActionDataTypes.ANSWER_PHONE_CALLS_PERMISSION -> context.doesHavePermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS)
        ActionDataTypes.TASKER_TASK -> false
        ActionDataTypes.SHORTCUT -> false
        ActionDataTypes.ACCESS_NOTIFICATION_POLICY -> {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        }
        ActionDataTypes.SECONDARY_GESTURE_SERVICE -> isAccessibilityServiceEnabled(
            context,
            TapGestureAccessibilityService::class.java
        )
        else -> true
    }
}

fun TapAction.isSupported(context: Context): Boolean {
    return when(this){
        TapAction.TASKER_EVENT, TapAction.TASKER_TASK -> context.isTaskerInstalled()
        else -> this.isAvailable
    }
}

private fun minSdk(api: Int): Boolean {
    return Build.VERSION.SDK_INT >= api
}
