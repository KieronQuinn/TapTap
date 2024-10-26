package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.delay
import org.koin.core.component.inject

class QuickSettingsExpandAction(
    serviceLifecycle: Lifecycle,
    context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private var isQuickSettingsOpen = false

    private val closeAction = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE
    }else{
        AccessibilityService.GLOBAL_ACTION_BACK
    }

    override val tag = "NotificationExpandAction"

    init {
        serviceLifecycle.whenCreated {
            accessibilityRouter.accessibilityOutputBus.collect {
                if(it is TapTapAccessibilityRouter.AccessibilityOutput.QuickSettingsShadeState){
                    isQuickSettingsOpen = it.open
                }
            }
        }
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showAccessibilityNotificationIfNeeded()) return
        if(isQuickSettingsOpen) {
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    closeAction
                )
            )
            delay(500L)
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    closeAction
                )
            )
        }else{
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
                )
            )
        }
    }

}