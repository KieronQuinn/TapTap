package com.kieronquinn.app.taptap.models

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Parcelable
import com.google.android.systemui.columbus.actions.Action
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.columbus.actions.*
import com.kieronquinn.app.taptap.core.TapServiceContainer
import com.kieronquinn.app.taptap.utils.extensions.deserialize
import com.kieronquinn.app.taptap.utils.extensions.getApplicationInfoOrNull
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
data class ActionInternal(val action: TapAction, val whenList : ArrayList<WhenGateInternal> = ArrayList(), var data: String? = null) : Parcelable {
    fun isBlocking(): Boolean {
        return whenList.isEmpty() && action.canBlock
    }

    fun getCardDescription(context: Context): CharSequence? {
        val formattedText = when(action.dataType){
            ActionDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfoOrNull(data)
                applicationInfo?.loadLabel(context.packageManager) ?: context.getString(R.string.item_action_app_uninstalled, data)
            }
            ActionDataTypes.SHORTCUT -> {
                val intent = Intent().apply {
                    deserialize(this@ActionInternal.data ?: "")
                }
                try {
                    context.packageManager.queryIntentActivities(intent, 0).firstOrNull()?.let {
                        val applicationInfo = context.packageManager.getApplicationInfoOrNull(it.activityInfo.packageName)
                        applicationInfo?.loadLabel(context.packageManager) ?: context.getString(R.string.item_action_app_uninstalled, it.activityInfo.packageName)
                    } ?: run {
                        null
                    }
                }catch (e: Exception){
                    null
                }
            }
            ActionDataTypes.TASKER_TASK -> {
                data
            }
            else -> null
        } ?: return null
        return context.getString(action.formattableDescription!!, formattedText)
    }

    fun getCardWhenListHeader(context: Context): String {
        return if(whenList.size > 1){
            context.getString(R.string.item_action_when_multiple)
        }else{
            context.getString(R.string.item_action_when)
        }
    }

    companion object {
        fun getActionForEnum(tapServiceContainer: TapServiceContainer, action: ActionInternal): Action? {
            val accessibilityService = tapServiceContainer.accessibilityService ?: return null
            val gestureAccessibilityService = tapServiceContainer.gestureAccessibilityService
            return try {
                when (action.action) {
                    TapAction.LAUNCH_CAMERA -> LaunchCamera(accessibilityService, action.whenList)
                    TapAction.BACK -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_BACK,
                        action.whenList
                    )
                    TapAction.HOME -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_HOME,
                        action.whenList
                    )
                    TapAction.LOCK_SCREEN -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN,
                        action.whenList
                    )
                    TapAction.RECENTS -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_RECENTS,
                        action.whenList
                    )
                    TapAction.SPLIT_SCREEN -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN,
                        action.whenList
                    )
                    TapAction.REACHABILITY -> LaunchReachability(accessibilityService, action.whenList)
                    TapAction.SCREENSHOT -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT,
                        action.whenList
                    )
                    TapAction.QUICK_SETTINGS -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS,
                        action.whenList
                    )
                    TapAction.NOTIFICATIONS -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS,
                        action.whenList
                    )
                    TapAction.POWER_DIALOG -> AccessibilityServiceGlobalAction(
                        accessibilityService,
                        AccessibilityService.GLOBAL_ACTION_POWER_DIALOG,
                        action.whenList
                    )
                    TapAction.FLASHLIGHT -> Flashlight(accessibilityService, action.whenList)
                    TapAction.LAUNCH_APP -> LaunchApp(accessibilityService, action.data ?: "", action.whenList)
                    TapAction.LAUNCH_SHORTCUT -> LaunchShortcut(
                        accessibilityService,
                        action.data ?: "",
                        action.whenList
                    )
                    TapAction.LAUNCH_ASSISTANT -> LaunchAssistant(accessibilityService, action.whenList)
                    TapAction.TASKER_EVENT -> TaskerEvent(accessibilityService, action.whenList)
                    TapAction.TASKER_TASK -> TaskerTask(accessibilityService, action.data ?: "", action.whenList)
                    TapAction.TOGGLE_PAUSE -> MusicAction(
                        accessibilityService,
                        MusicAction.Command.TOGGLE_PAUSE,
                        action.whenList
                    )
                    TapAction.PREVIOUS -> MusicAction(
                        accessibilityService,
                        MusicAction.Command.PREVIOUS,
                        action.whenList
                    )
                    TapAction.NEXT -> MusicAction(accessibilityService, MusicAction.Command.NEXT, action.whenList)
                    TapAction.VOLUME_PANEL -> VolumeAction(
                        accessibilityService,
                        AudioManager.ADJUST_SAME,
                        action.whenList
                    )
                    TapAction.VOLUME_UP -> VolumeAction(
                        accessibilityService,
                        AudioManager.ADJUST_RAISE,
                        action.whenList
                    )
                    TapAction.VOLUME_DOWN -> VolumeAction(
                        accessibilityService,
                        AudioManager.ADJUST_LOWER,
                        action.whenList
                    )
                    TapAction.VOLUME_TOGGLE_MUTE -> VolumeAction(
                        accessibilityService,
                        AudioManager.ADJUST_TOGGLE_MUTE,
                        action.whenList
                    )
                    TapAction.ALARM_TIMER -> AlarmTimerAction(accessibilityService, action.whenList)
                    TapAction.ALARM_SNOOZE -> AlarmSnoozeAction(accessibilityService, action.whenList)
                    TapAction.SOUND_PROFILER -> SoundProfileAction(accessibilityService, action.whenList)
                    TapAction.WAKE_DEVICE -> WakeDeviceAction(accessibilityService, action.whenList)
                    TapAction.GOOGLE_VOICE_ACCESS -> GoogleVoiceAccessAction(accessibilityService, action.whenList)
                    TapAction.LAUNCH_SEARCH -> LaunchSearch(accessibilityService, action.whenList)
                    TapAction.HAMBURGER -> HamburgerAction(gestureAccessibilityService ?: return null, action.whenList)
                    TapAction.APP_DRAWER -> AccessibilityServiceGlobalAction(accessibilityService, 14, action.whenList)
                    TapAction.ALT_TAB -> AltTabAction(accessibilityService, action.whenList)
                    TapAction.ACCESSIBILITY_BUTTON_CHOOSER -> AccessibilityServiceGlobalAction(accessibilityService, 12, action.whenList)
                    TapAction.ACCESSIBILITY_SHORTCUT -> AccessibilityServiceGlobalAction(accessibilityService, 13, action.whenList)
                    TapAction.ACCESSIBILITY_BUTTON -> AccessibilityServiceGlobalAction(accessibilityService, 11, action.whenList)
                    TapAction.ACCEPT_CALL -> AcceptCall(accessibilityService, action.whenList)
                    TapAction.REJECT_CALL -> RejectCall(accessibilityService, action.whenList)
                    TapAction.SWIPE_UP -> SwipeAction(gestureAccessibilityService ?: return null, SwipeAction.SwipeDirection.UP, action.whenList)
                    TapAction.SWIPE_DOWN -> SwipeAction(gestureAccessibilityService ?: return null, SwipeAction.SwipeDirection.DOWN, action.whenList)
                    TapAction.SWIPE_LEFT -> SwipeAction(gestureAccessibilityService ?: return null, SwipeAction.SwipeDirection.LEFT, action.whenList)
                    TapAction.SWIPE_RIGHT -> SwipeAction(gestureAccessibilityService ?: return null, SwipeAction.SwipeDirection.RIGHT, action.whenList)
                }
            } catch (e: RuntimeException) {
                //Enum not found, probably a downgrade issue
                null
            }
        }
    }
}