package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import android.util.Log
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.TapAccessibilityService

class AppVisibility(context: Context, private val packageName: String) : Gate(context) {

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val tapAccessibilityService = context as TapAccessibilityService
        return tapAccessibilityService.getCurrentPackageName() == packageName
    }

}