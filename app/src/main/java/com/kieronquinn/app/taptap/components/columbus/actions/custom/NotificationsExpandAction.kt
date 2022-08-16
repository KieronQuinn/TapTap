package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import org.koin.core.component.inject

class NotificationsExpandAction(
    serviceLifecycle: Lifecycle,
    context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private var isNotificationShadeOpen = false

    private val closeAction = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE
    }else{
        AccessibilityService.GLOBAL_ACTION_BACK
    }

    override val tag = "NotificationExpandAction"

    init {
        serviceLifecycle.coroutineScope.launchWhenCreated {
            accessibilityRouter.accessibilityOutputBus.collect {
                if(it is TapTapAccessibilityRouter.AccessibilityOutput.NotificationShadeState){
                    isNotificationShadeOpen = it.open
                }
            }
        }
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showAccessibilityNotificationIfNeeded()) return
        val isShadeOpen = isNotificationShadeOpen
        accessibilityRouter.postInput(
            TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                if(isShadeOpen) closeAction else AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
            )
        )
    }

}