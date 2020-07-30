//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.kieronquinn.app.taptap.utils

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.topjohnwu.superuser.Shell
import kotlin.jvm.internal.Intrinsics

class AccessibilityServiceGlobalAction(private val accessiblityService: AccessibilityService, private val globalAction: Int) : Action(accessiblityService, null) {

    fun setTriggerListener(listener: TriggerListener?) {
        triggerListener = listener
    }

    interface TriggerListener {
        fun onTrigger()
    }

    private var triggerListener: TriggerListener? =
        null

    override fun isAvailable(): Boolean {
        return true
    }

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var1 == 3) {
            onTrigger()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
        if (triggerListener != null) triggerListener?.onTrigger()
        Log.d("TAS", "onTrigger accessiblityservice $globalAction")
        accessiblityService.performGlobalAction(globalAction)
    }

    override fun toString(): String {
        val var1 = StringBuilder()
        var1.append(super.toString())
        var1.append("]")
        return var1.toString()
    }

    init {
        Intrinsics.checkParameterIsNotNull(accessiblityService, "context")
    }
}