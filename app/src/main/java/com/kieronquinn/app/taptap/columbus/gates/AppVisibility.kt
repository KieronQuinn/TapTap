package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import android.os.Handler
import android.util.Log
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.gates.TransientGate
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isPackageCamera

class AppVisibility(context: Context, private val packageName: String) : Gate(context) {

    init {
        Log.d("AppVisibility", "START $packageName")
    }

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val tapAccessibilityService = context as TapAccessibilityService
        Log.d("AppVisibility", "isBlocked ${tapAccessibilityService.getCurrentPackageName() == packageName} ${tapAccessibilityService.getCurrentPackageName()}")
        return tapAccessibilityService.getCurrentPackageName() == packageName
    }

}