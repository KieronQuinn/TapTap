package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.accessibilityservice.AccessibilityService
import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import kotlinx.coroutines.delay
import org.koin.core.component.inject

class AltTabAction(
    serviceLifecycle: Lifecycle,
    context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()

    override val tag = "AltTabAction"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showAccessibilityNotificationIfNeeded()) return
        accessibilityRouter.postInput(
            TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                AccessibilityService.GLOBAL_ACTION_RECENTS
            )
        )
        delay(300)
        accessibilityRouter.postInput(
            TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                AccessibilityService.GLOBAL_ACTION_RECENTS
            )
        )
    }

}