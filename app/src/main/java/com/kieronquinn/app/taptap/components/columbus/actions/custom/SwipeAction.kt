package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import org.koin.core.component.inject

class SwipeAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val direction: TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()

    override val tag = "SwipeAction"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showGestureAccessibilityNotificationIfNeeded()) return
        accessibilityRouter.postInput(TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe(direction))
    }

}