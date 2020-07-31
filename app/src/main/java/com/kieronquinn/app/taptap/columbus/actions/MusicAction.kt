package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor


class MusicAction(context: Context, private val command: Command) : ActionBase(context) {

    enum class Command(val rawCommand: Int) {
        TOGGLE_PAUSE(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
        PREVIOUS(KeyEvent.KEYCODE_MEDIA_PREVIOUS),
        NEXT(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

    override fun isAvailable(): Boolean {
        return audioManager != null
    }

    override fun onTrigger() {
        super.onTrigger()
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, command.rawCommand)
        audioManager?.dispatchMediaKeyEvent(eventDown)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, command.rawCommand)
        audioManager?.dispatchMediaKeyEvent(eventUp)
    }


}