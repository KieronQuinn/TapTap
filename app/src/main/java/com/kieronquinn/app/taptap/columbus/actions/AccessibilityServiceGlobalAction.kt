package com.kieronquinn.app.taptap.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.columbus.actions.ActionBase
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import kotlin.jvm.internal.Intrinsics

class AccessibilityServiceGlobalAction(private val accessiblityService: TapAccessibilityService, private val globalAction: Int, whenGates: List<WhenGateInternal>) : ActionBase(accessiblityService, whenGates) {

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
        if (globalAction == AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS && accessiblityService.isNotificationShadeOpen) {
            accessiblityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Handler().postDelayed({
                accessiblityService.isNotificationShadeOpen = false
            }, 500)
            return
        }
        if (globalAction == AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS && accessiblityService.isQuickSettingsOpen) {
            accessiblityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Handler().postDelayed({
                accessiblityService.isQuickSettingsOpen = false
                //This drops back to the notifications so we need to handle that too
                accessiblityService.isNotificationShadeOpen = true
            }, 500)
            return
        }
        //Override wake device if we're sending device to sleep
        if (globalAction == AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) {
            (context.applicationContext as? TapTapApplication)?.disableWake = true
        }
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