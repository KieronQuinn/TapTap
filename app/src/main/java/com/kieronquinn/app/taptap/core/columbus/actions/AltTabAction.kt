package com.kieronquinn.app.taptap.core.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService

class AltTabAction(private val accessiblityService: TapAccessibilityService, whenGates: List<WhenGateInternal>) : ActionBase(accessiblityService, whenGates) {

    override fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?) {
        super.onTrigger(detectionProperties)
        accessiblityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        Handler().postDelayed({
            accessiblityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }, 300)
    }

}