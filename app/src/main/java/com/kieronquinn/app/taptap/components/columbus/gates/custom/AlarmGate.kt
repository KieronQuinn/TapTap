package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.isAudioStreamActive

class AlarmGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    override fun isBlocked(): Boolean {
        return isAudioStreamActive(
            AudioManager.STREAM_ALARM
        )
    }

}