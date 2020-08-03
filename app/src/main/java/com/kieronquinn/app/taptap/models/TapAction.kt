package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.utils.AccessibilityServiceGlobalAction
import com.kieronquinn.app.taptap.utils.LaunchCameraLocal
import com.kieronquinn.app.taptap.utils.minApi

enum class TapAction(val clazz: Class<*>, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, val isAvailable: Boolean, val isWhenAvailable: Boolean, @StringRes val formattableDescription: Int? = null, val dataType: ActionDataTypes? = null) {
    LAUNCH_ASSISTANT(LaunchAssistant::class.java, R.string.action_launch_assistant, R.string.action_launch_assistant_desc, R.drawable.ic_launch_assistant, true, true),
    LAUNCH_CAMERA(LaunchCameraLocal::class.java, R.string.action_launch_camera, R.string.action_launch_camera_desc, R.drawable.ic_camera_visibility, true, true, dataType = ActionDataTypes.CAMERA_PERMISSION),
    HOME(AccessibilityServiceGlobalAction::class.java, R.string.action_home, R.string.action_home_desc, R.drawable.ic_action_home, true, true),
    BACK(AccessibilityServiceGlobalAction::class.java, R.string.action_back, R.string.action_back_desc, R.drawable.ic_action_back, true, true),
    RECENTS(AccessibilityServiceGlobalAction::class.java, R.string.action_recents, R.string.action_recents_desc, R.drawable.ic_action_recent, true, true),
    NOTIFICATIONS(AccessibilityServiceGlobalAction::class.java, R.string.action_notifications, R.string.action_notifications_desc, R.drawable.ic_action_notifications, true, true),
    QUICK_SETTINGS(AccessibilityServiceGlobalAction::class.java, R.string.action_quick_settings, R.string.action_quick_settings_desc, R.drawable.ic_action_quick_settings, true, true),
    SCREENSHOT(AccessibilityServiceGlobalAction::class.java, R.string.action_screenshot, R.string.action_screenshot_desc, R.drawable.ic_action_screenshot, minApi(28), true),
    LOCK_SCREEN(AccessibilityServiceGlobalAction::class.java, R.string.action_lock_screen, R.string.action_lock_screen_desc, R.drawable.ic_power_state, minApi(28), true),
    SPLIT_SCREEN(AccessibilityServiceGlobalAction::class.java, R.string.action_split_screen, R.string.action_split_screen_desc, R.drawable.ic_action_split_screen, true, true),
    FLASHLIGHT(AccessibilityServiceGlobalAction::class.java, R.string.action_flashlight, R.string.action_flashlight_desc, R.drawable.ic_action_category_utilities, true, true, dataType = ActionDataTypes.CAMERA_PERMISSION),
    LAUNCH_APP(LaunchApp::class.java, R.string.action_launch_app, R.string.action_launch_app_desc, R.drawable.ic_action_category_launch, true, true, R.string.action_launch_app_desc_formattable, ActionDataTypes.PACKAGE_NAME),
    LAUNCH_SHORTCUT(LaunchApp::class.java, R.string.action_launch_shortcut, R.string.action_launch_shortcut_desc, R.drawable.ic_action_shortcut, true, true, R.string.action_launch_shortcut_desc_formattable, ActionDataTypes.SHORTCUT),
    TASKER_EVENT(TaskerEvent::class.java, R.string.action_tasker_event, R.string.action_tasker_event_desc, R.drawable.ic_action_tasker, true, true),
    TASKER_TASK(TaskerTask::class.java, R.string.action_tasker_task, R.string.action_tasker_task_desc, R.drawable.ic_action_tasker, true, true, dataType = ActionDataTypes.TASKER_TASK),
    TOGGLE_PAUSE(MusicAction::class.java, R.string.action_toggle_pause, R.string.action_toggle_pause_desc, R.drawable.ic_action_toggle_pause, true, true),
    PREVIOUS(MusicAction::class.java, R.string.action_previous, R.string.action_previous_desc, R.drawable.ic_action_previous, true, true),
    NEXT(MusicAction::class.java, R.string.action_next, R.string.action_next_desc, R.drawable.ic_action_next, true, true),
    VOLUME_PANEL(VolumeAction::class.java, R.string.action_volume_panel, R.string.action_volume_panel_desc, R.drawable.ic_action_volume_panel, true, true),
    VOLUME_UP(VolumeAction::class.java, R.string.action_volume_up, R.string.action_volume_up_desc, R.drawable.ic_action_volume_up, true, true),
    VOLUME_DOWN(VolumeAction::class.java, R.string.action_volume_down, R.string.action_volume_down_desc, R.drawable.ic_action_volume_down, true, true),
    VOLUME_TOGGLE_MUTE(VolumeAction::class.java, R.string.action_volume_toggle_mute, R.string.action_volume_toggle_mute_desc, R.drawable.ic_action_volume_toggle_mute, true, true),
    WAKE_DEVICE(WakeDeviceAction::class.java, R.string.action_wake_device, R.string.action_wake_device_desc, R.drawable.ic_wake_from_sleep, true, true)
}

enum class ActionDataTypes {
    PACKAGE_NAME,
    CAMERA_PERMISSION,
    TASKER_TASK,
    SHORTCUT
}