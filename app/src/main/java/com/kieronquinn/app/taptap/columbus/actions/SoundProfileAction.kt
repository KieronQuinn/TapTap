package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.media.AudioManager

class SoundProfileAction(context: Context) : ActionBase(context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentProfileAction = audioManager.ringerMode

    override fun isAvailable(): Boolean {
        return true
    }

    override fun onTrigger() {
        super.onTrigger()
        currentProfileAction = audioManager.ringerMode
        when (currentProfileAction) {
            0 -> {
                audioManager.setRingerMode(1)
            }
            1 -> {
                audioManager.setRingerMode(2)
            }
            2 -> {
                audioManager.setRingerMode(0)
            }
        }
    }
}