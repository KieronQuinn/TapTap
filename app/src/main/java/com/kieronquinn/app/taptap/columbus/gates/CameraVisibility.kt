package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.gates.TransientGate
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isPackageCamera

class CameraVisibility(context: Context) : Gate(context) {

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val tapAccessibilityService = context as TapAccessibilityService
        return context.isPackageCamera(tapAccessibilityService.getCurrentPackageName())
    }

}