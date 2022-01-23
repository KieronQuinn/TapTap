package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.utils.extensions.startActivity

class LaunchDeviceControlsAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>,
    private val service: TapTapRootServiceRepository
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects, requiresWake = true
) {

    companion object {
        val DEVICE_CONTROLS_INTENT = Intent().apply {
            `package` = "com.android.systemui"
            component = ComponentName("com.android.systemui", "com.android.systemui.controls.ui.ControlsActivity")
            //extra required for the activity to actually display content
            putExtra("extra_animate", true)
        }
    }

    private val deviceControlsAvailable by lazy {
        context.packageManager.resolveActivity(DEVICE_CONTROLS_INTENT, 0) != null
    }

    override val tag = "DeviceControls"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        service.runWithService {
            it.startActivity(context, DEVICE_CONTROLS_INTENT, omitThread = true, enterResId = R.anim.fade_in, R.anim.fade_out)
        } ?: showRootNotification()
    }

    override fun isAvailable(): Boolean {
        return deviceControlsAvailable && super.isAvailable()
    }

}