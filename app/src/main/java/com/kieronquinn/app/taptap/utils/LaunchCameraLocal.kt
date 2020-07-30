//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.kieronquinn.app.taptap.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.TapAccessibilityService
import kotlin.jvm.internal.Intrinsics

class LaunchCameraLocal(var1: Context) : Action(var1, null) {
    private val cameraAvailable: Boolean
    fun setTriggerListener(listener: TriggerListener?) {
        triggerListener = listener
    }

    interface TriggerListener {
        fun onTrigger()
    }

    private var triggerListener: TriggerListener? =
        null

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return cameraAvailable && !context.isPackageCamera(accessibilityService.getCurrentPackageName())
    }

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var1 == 3) {
            onTrigger()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
        Log.d("XColumbus", "onTrigger")
        if (triggerListener != null) triggerListener!!.onTrigger()
        context
            .startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun toString(): String {
        val var1 = StringBuilder()
        var1.append(super.toString())
        var1.append(" [cameraAvailable -> ")
        var1.append(cameraAvailable)
        var1.append("]")
        return var1.toString()
    }

    init {
        Intrinsics.checkParameterIsNotNull(var1, "context")
        cameraAvailable = var1.packageManager.hasSystemFeature("android.hardware.camera")
    }
}