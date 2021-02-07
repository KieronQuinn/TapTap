package com.kieronquinn.app.taptap.core.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.os.Handler
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import kotlin.jvm.internal.Intrinsics

class AccessibilityServiceGlobalAction(private val accessibilityService: TapAccessibilityService, private val globalAction: Int, whenGates: List<WhenGateInternal>) : ActionBase(accessibilityService, whenGates) {

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
        if (globalAction == AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS && accessibilityService.isNotificationShadeOpen) {
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Handler().postDelayed({
                accessibilityService.isNotificationShadeOpen = false
            }, 500)
            return
        }
        if (globalAction == AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS && accessibilityService.isQuickSettingsOpen) {
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            Handler().postDelayed({
                accessibilityService.isQuickSettingsOpen = false
                //This drops back to the notifications so we need to handle that too
                accessibilityService.isNotificationShadeOpen = true
            }, 500)
            return
        }
        //Override wake device if we're sending device to sleep
        if (globalAction == AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) {
            (context.applicationContext as? TapTapApplication)?.disableWake = true
        }
        accessibilityService.performGlobalAction(globalAction)
    }

    override fun toString(): String {
        val var1 = StringBuilder()
        var1.append(super.toString())
        var1.append("]")
        return var1.toString()
    }

    init {
        Intrinsics.checkParameterIsNotNull(accessibilityService, "context")
    }
}