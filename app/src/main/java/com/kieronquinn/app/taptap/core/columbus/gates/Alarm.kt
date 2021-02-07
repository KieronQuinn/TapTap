package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import android.media.AudioManager
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.utils.extensions.isAudioStreamActive

class Alarm(context: Context): Gate(context) {

    override fun isBlocked(): Boolean  =
        isAudioStreamActive(
            AudioManager.STREAM_ALARM
        )

    override fun onActivate() {}
    override fun onDeactivate() {}

}