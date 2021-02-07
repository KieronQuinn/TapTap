package com.kieronquinn.app.taptap.core.columbus.actions

import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.getStaticStatusBarHeight
import com.kieronquinn.app.taptap.utils.extensions.px


class HamburgerAction(private val gestureAccessibilityService: TapGestureAccessibilityService, whenGates: List<WhenGateInternal>) : ActionBase(gestureAccessibilityService, whenGates) {

    override fun onTrigger() {
        gestureAccessibilityService?.rootInActiveWindow?.run {
            val gesture = createClick(25f.px, getStaticStatusBarHeight(
                context
            ) + 25f.px)
            gestureAccessibilityService.dispatchGesture(gesture, null, null)
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

    override fun isAvailable(): Boolean {
        return true
    }
}