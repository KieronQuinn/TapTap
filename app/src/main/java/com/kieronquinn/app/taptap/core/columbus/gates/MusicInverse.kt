package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context

class MusicInverse(context: Context) : Music(context) {

    override fun isBlocked(): Boolean {
        return !super.isBlocked()
    }

}