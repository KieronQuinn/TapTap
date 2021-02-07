package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import android.media.AudioManager
import com.google.android.systemui.columbus.gates.Gate

open class Music(context: Context) : Gate(context) {

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        return audioManager.isMusicActive
    }

}