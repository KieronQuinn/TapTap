package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.ui.activities.ReachabilityActivity
import com.kieronquinn.app.taptap.utils.extensions.isActivityRunning
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.inject

class LaunchReachabilityAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "LaunchReachabilityAction"

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val currentApp = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, "")

    init {
        lifecycle.whenCreated {
            currentApp.collect()
        }
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showAccessibilityNotificationIfNeeded()) return
        if(context.isActivityRunning(ReachabilityActivity::class.java)){
            //Just close split screen
            accessibilityRouter.postInput(TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
            ))
        }else{
            val intent = Intent(context, ReachabilityActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                putExtra(ReachabilityActivity.KEY_PACKAGE_NAME, currentApp.value)
            }
            context.startActivity(intent)
        }
    }

}