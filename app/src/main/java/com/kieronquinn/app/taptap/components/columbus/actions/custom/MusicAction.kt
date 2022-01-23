package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate

class MusicAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val command: Command,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "MusicAction"

    enum class Command(val rawCommand: Int) {
        TOGGLE_PAUSE(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
        PREVIOUS(KeyEvent.KEYCODE_MEDIA_PREVIOUS),
        NEXT(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, command.rawCommand)
        audioManager.dispatchMediaKeyEvent(eventDown)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, command.rawCommand)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }

}