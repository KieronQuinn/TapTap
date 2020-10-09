package com.kieronquinn.app.taptap.columbus.actions

import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.getStaticStatusBarHeight
import com.kieronquinn.app.taptap.utils.px
import kotlin.jvm.internal.Intrinsics


class HamburgerAction(private val accessiblityService: TapAccessibilityService, whenGates: List<WhenGateInternal>) : ActionBase(accessiblityService, whenGates) {

    override fun onTrigger() {
        accessiblityService.gestureAccessibilityService?.rootInActiveWindow?.run {
            val gesture = createClick(25f.px, getStaticStatusBarHeight(context) + 25f.px)
            accessiblityService.gestureAccessibilityService?.dispatchGesture(gesture, null, null)
        }
    }

    private fun createClick(x: Float, y: Float): GestureDescription {
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val clickStroke = StrokeDescription(clickPath, 0, 1L)
        val clickBuilder = GestureDescription.Builder()
        clickBuilder.addStroke(clickStroke)
        return clickBuilder.build()
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

    override fun isAvailable(): Boolean {
        return accessiblityService.gestureAccessibilityService != null
    }
}