package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.ui.activities.FlashlightToggleActivity

class FlashlightAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "FlashlightAction"

    override fun isAvailable(): Boolean {
        if(!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return false
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        context.startActivity(Intent(context, FlashlightToggleActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

}