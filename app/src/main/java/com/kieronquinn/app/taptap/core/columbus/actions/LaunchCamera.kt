//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.kieronquinn.app.taptap.core.columbus.actions

import android.app.KeyguardManager
import android.content.Context
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.getCameraLaunchIntent
import com.kieronquinn.app.taptap.utils.extensions.isPackageCamera
import kotlin.jvm.internal.Intrinsics

class LaunchCamera(var1: Context, whenGates: List<WhenGateInternal>) : ActionBase(var1, whenGates) {

    private val keyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    private val cameraAvailable: Boolean

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return cameraAvailable && !context.isPackageCamera(accessibilityService.getCurrentPackageName()) && super.isAvailable()
    }

    override fun onTrigger() {
        val isSecure = keyguardManager.isKeyguardSecure
        val cameraIntent = getCameraLaunchIntent(isSecure)
        context.startActivity(cameraIntent)
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