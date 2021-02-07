package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.content.Intent
import com.kieronquinn.app.taptap.models.WhenGateInternal

class GoogleVoiceAccessAction(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override fun onTrigger() {
        context.sendBroadcast(Intent("com.google.android.apps.accessibility.voiceaccess.ACTIVATE"))
    }

    override fun toString(): String {
        val var1 = StringBuilder()
        var1.append(super.toString())
        var1.append("]")
        return var1.toString()
    }
}