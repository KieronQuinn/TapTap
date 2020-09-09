package com.kieronquinn.app.taptap.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.util.Log
import com.kieronquinn.app.taptap.columbus.actions.ActionBase
import com.kieronquinn.app.taptap.models.WhenGateInternal
import kotlin.jvm.internal.Intrinsics

class AccessibilityServiceGlobalAction(private val accessiblityService: AccessibilityService, private val globalAction: Int, whenGates: List<WhenGateInternal>) : ActionBase(accessiblityService, whenGates) {

    @SuppressLint("WrongConstant")
    override fun onTrigger() {
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