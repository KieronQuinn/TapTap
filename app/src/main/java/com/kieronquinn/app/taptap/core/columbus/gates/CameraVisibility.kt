package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.isPackageCamera

class CameraVisibility(context: Context) : Gate(context) {

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val tapAccessibilityService = context as TapAccessibilityService
        return context.isPackageCamera(tapAccessibilityService.getCurrentPackageName())
    }

}