package com.kieronquinn.app.taptap.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.taptap.columbus.actions.ActionBase
import com.kieronquinn.app.taptap.models.WhenGateInternal
import kotlin.jvm.internal.Intrinsics

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