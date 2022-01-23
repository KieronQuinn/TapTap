package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate

class VolumeAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val action: Int,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "VolumeAction"

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        audioManager.adjustVolume(action, AudioManager.FLAG_SHOW_UI)
    }

}