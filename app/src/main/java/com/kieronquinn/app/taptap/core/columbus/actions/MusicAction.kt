package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.kieronquinn.app.taptap.models.WhenGateInternal


class MusicAction(context: Context, private val command: Command, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    enum class Command(val rawCommand: Int) {
        TOGGLE_PAUSE(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
        PREVIOUS(KeyEvent.KEYCODE_MEDIA_PREVIOUS),
        NEXT(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

    override fun isAvailable(): Boolean {
        return audioManager != null && super.isAvailable()
    }

    override fun onTrigger() {
        super.onTrigger()
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, command.rawCommand)
        audioManager?.dispatchMediaKeyEvent(eventDown)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, command.rawCommand)
        audioManager?.dispatchMediaKeyEvent(eventUp)
    }


}