package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.services.TapForegroundService

class AppVisibility(context: Context, private val packageName: String) : Gate(context) {

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        return if(context is TapForegroundService) {
            val tapAccessibilityService = context as TapForegroundService
            tapAccessibilityService.getCurrentPackageName() == packageName
        }else{
            val tapAccessibilityService = context as TapAccessibilityService
            tapAccessibilityService.getCurrentPackageName() == packageName
        }
    }

}