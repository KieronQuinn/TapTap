package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.isPackageAssistant
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject

class LaunchAssistantAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects, requiresWake = true
) {

    override val tag = "LaunchAssistantAction"
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { context.isPackageAssistant((it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName) }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycleScope.launchWhenCreated {
            isOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        if(isOpen.value) {
            return false
        }
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        try {
            val launchIntent = Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}