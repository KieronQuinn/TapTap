package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate

class MusicInverseGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun isBlocked(): Boolean {
        return !audioManager.isMusicActive
    }

}