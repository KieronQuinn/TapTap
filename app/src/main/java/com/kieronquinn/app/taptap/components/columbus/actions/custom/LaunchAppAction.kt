package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.isAppLaunchable
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.inject

class LaunchAppAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val appPackageName: String,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects, requiresWake = true
) {

    override val tag = "LaunchAppAction"
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName == appPackageName }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycle.whenCreated {
            isOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        if(isOpen.value) return false
        if(!context.isAppLaunchable(appPackageName)) return false
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        val packageManager = context.packageManager
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}