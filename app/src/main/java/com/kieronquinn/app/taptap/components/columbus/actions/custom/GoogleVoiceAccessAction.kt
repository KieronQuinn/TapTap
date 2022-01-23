package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.isPackageInstalled

class GoogleVoiceAccessAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    companion object {
        private const val ACCESSIBILITY_PACKAGE_NAME = "com.google.android.marvin.talkback"
        private const val ACCESSIBILITY_ACTION = "com.google.android.apps.accessibility.voiceaccess.ACTIVATE"
    }

    override val tag = "GoogleVoiceAccessAction"

    override fun isAvailable(): Boolean {
        if(!context.isPackageInstalled(ACCESSIBILITY_PACKAGE_NAME)) return false
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        context.sendBroadcast(Intent(ACCESSIBILITY_ACTION).apply {
            `package` = ACCESSIBILITY_PACKAGE_NAME
        })
    }

}