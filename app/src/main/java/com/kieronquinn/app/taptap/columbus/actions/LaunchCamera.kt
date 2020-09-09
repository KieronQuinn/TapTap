//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.kieronquinn.app.taptap.columbus.actions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.getCameraLaunchIntent
import com.kieronquinn.app.taptap.utils.isPackageCamera
import kotlin.jvm.internal.Intrinsics

class LaunchCamera(var1: Context, whenGates: List<WhenGateInternal>) : ActionBase(var1, whenGates) {
    private val cameraAvailable: Boolean

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return cameraAvailable && !context.isPackageCamera(accessibilityService.getCurrentPackageName()) && super.isAvailable()
    }

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
        val cameraIntent = context.getCameraLaunchIntent()
        if(cameraIntent.size > 1){
            val chooser = Intent.createChooser(cameraIntent.removeAt(0), context.getString(R.string.picker_launch_camera))
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntent.toTypedArray())
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }else if(cameraIntent.isNotEmpty()){
            context.startActivity(cameraIntent.first())
        }else{
            //No camera app available
        }
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