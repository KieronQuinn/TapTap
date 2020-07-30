package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.actions.LaunchApp
import com.kieronquinn.app.taptap.columbus.actions.LaunchAssistant
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
    FLASHLIGHT(AccessibilityServiceGlobalAction::class.java, R.string.action_flashlight, R.string.action_flashlight_desc, R.drawable.ic_action_category_utilities, true, true, dataType = ActionDataTypes.CAMERA_PERMISSION),
    LAUNCH_APP(LaunchApp::class.java, R.string.action_launch_app, R.string.action_launch_app_desc, R.drawable.ic_action_category_launch, true, true, R.string.action_launch_app_desc_formattable, ActionDataTypes.PACKAGE_NAME)
}

enum class ActionDataTypes {
    PACKAGE_NAME,
    CAMERA_PERMISSION
}