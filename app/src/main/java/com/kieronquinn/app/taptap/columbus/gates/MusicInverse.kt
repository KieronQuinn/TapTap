package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import com.google.android.systemui.columbus.gates.Gate

class MusicInverse(context: Context) : Music(context) {

    override fun isBlocked(): Boolean {
        return !super.isBlocked()
    }

}