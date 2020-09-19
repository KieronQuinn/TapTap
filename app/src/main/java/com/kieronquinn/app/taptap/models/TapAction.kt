package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.columbus.actions.AccessibilityServiceGlobalAction
import com.kieronquinn.app.taptap.columbus.actions.LaunchCamera
import com.kieronquinn.app.taptap.utils.minSdk

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
    SCREENSHOT(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_screenshot, R.string.action_screenshot_desc, R.drawable.ic_action_screenshot, minSdk(28), true, true),
    NOTIFICATIONS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_notifications, R.string.action_notifications_desc, R.drawable.ic_action_notifications, true, true, true),
    QUICK_SETTINGS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_quick_settings, R.string.action_quick_settings_desc, R.drawable.ic_action_quick_settings, true, true, true),
    LOCK_SCREEN(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_lock_screen, R.string.action_lock_screen_desc, R.drawable.ic_power_state, minSdk(28), true, true),
    WAKE_DEVICE(WakeDeviceAction::class.java, TapActionCategory.ACTIONS, R.string.action_wake_device, R.string.action_wake_device_desc, R.drawable.ic_wake_from_sleep, true, true, false),
    HOME(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_home, R.string.action_home_desc, R.drawable.ic_action_home, true, true, true),
    BACK(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_back, R.string.action_back_desc, R.drawable.ic_action_back, true, true, true),
    RECENTS(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_recents, R.string.action_recents_desc, R.drawable.ic_action_recent, true, true, true),
    SPLIT_SCREEN(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_split_screen, R.string.action_split_screen_desc, R.drawable.ic_action_split_screen, true, true, true),
    REACHABILITY(LaunchReachability::class.java, TapActionCategory.UTILITIES, R.string.action_reachability, R.string.action_reachability_desc, R.drawable.ic_action_reachability, true, true, true),
    POWER_DIALOG(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_power_dialog, R.string.action_power_dialog_desc, R.drawable.ic_action_power_dialog, true, true, true),
    APP_DRAWER(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_app_drawer, R.string.action_app_drawer_desc, R.drawable.ic_action_app_drawer, minSdk(30), true, true),
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
    GOOGLE_VOICE_ACCESS(GoogleVoiceAccessAction::class.java, TapActionCategory.ACTIONS, R.string.action_google_voice_access, R.string.action_google_voice_access_desc, R.drawable.ic_action_google_voice_access, true, true, true),
    ACCESSIBILITY_BUTTON(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_button, R.string.action_accessibility_button_desc, R.drawable.ic_action_accessibility, minSdk(30), true, true),
    ACCESSIBILITY_BUTTON_CHOOSER(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_button_chooser, R.string.action_accessibility_button_chooser_desc, R.drawable.ic_action_accessibility, minSdk(30), true, true),
    ACCESSIBILITY_SHORTCUT(AccessibilityServiceGlobalAction::class.java, TapActionCategory.ACTIONS, R.string.action_accessibility_shortcut, R.string.action_accessibility_shortcut_desc, R.drawable.ic_action_accessibility, minSdk(30), true, true),
    HAMBURGER(HamburgerAction::class.java, TapActionCategory.ADVANCED, R.string.action_hamburger, R.string.action_hamburger_desc, R.drawable.ic_action_hamburger, minSdk(24), true, true)
}

enum class ActionDataTypes {
    PACKAGE_NAME,
    CAMERA_PERMISSION,
    TASKER_TASK,
    SHORTCUT,
    ACCESS_NOTIFICATION_POLICY
}
