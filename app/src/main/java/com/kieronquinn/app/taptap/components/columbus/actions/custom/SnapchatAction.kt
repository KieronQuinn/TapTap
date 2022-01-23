package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.isAppLaunchable
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject

class SnapchatAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects, requiresWake = true
) {

    companion object {
        const val PACKAGE_NAME = "com.snapchat.android"
        val LAUNCH_COMPONENT = ComponentName("com.snapchat.android", "com.snap.samples.catalina.CatalinaActivity")
    }

    override val tag = "SnapchatAction"

    private val keyguardManager by lazy {
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    private val launchIntent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName == PACKAGE_NAME }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycleScope.launchWhenCreated {
            isOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        if(isOpen.value) return false
        if(!context.isAppLaunchable(PACKAGE_NAME)) return false
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        if(keyguardManager.isKeyguardLocked) {
            try {
                context.startActivity(Intent().apply {
                    component = LAUNCH_COMPONENT
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: ActivityNotFoundException) {
                //Ignore
            }
        }else{
            try {
                context.startActivity(launchIntent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } ?: return)
            } catch (e: ActivityNotFoundException) {
                //Ignore
            }
        }
    }

}