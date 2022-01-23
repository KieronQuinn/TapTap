package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate

class SoundProfileAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "SoundProfileAction"

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var currentProfileAction = audioManager.ringerMode

    private val isNotificationAccessGranted: Boolean by lazy {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.isNotificationPolicyAccessGranted
    }

    override fun isAvailable(): Boolean {
        return isNotificationAccessGranted && super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        currentProfileAction = audioManager.ringerMode
        when (currentProfileAction) {
            0 -> {
                audioManager.ringerMode = 1
            }
            1 -> {
                audioManager.ringerMode = 2
            }
            2 -> {
                audioManager.ringerMode = 0
            }
        }
    }

}