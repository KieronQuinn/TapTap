package com.kieronquinn.app.taptap.core.columbus.actions

import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.getStaticStatusBarHeight
import com.kieronquinn.app.taptap.utils.extensions.px
import kotlin.math.roundToInt


class SwipeAction(private val gestureAccessibilityService: TapGestureAccessibilityService, private val swipeDirection: SwipeDirection, whenGates: List<WhenGateInternal>) : ActionBase(gestureAccessibilityService, whenGates) {

    private val displayMetrics by lazy {
        gestureAccessibilityService.resources.displayMetrics
    }

    private val height by lazy {
        displayMetrics.heightPixels
    }

    private val width by lazy {
        displayMetrics.widthPixels
    }

    private val swipeUpGesture by lazy {
        val gestureBuilder = GestureDescription.Builder()
        val quarterHeight = height * 0.45
        val threeQuarterHeight = height * 0.55
        val halfWidth = width * 0.5
        val path = Path().apply {
            moveTo(halfWidth.toFloat(), threeQuarterHeight.toFloat())
            lineTo(halfWidth.toFloat(), quarterHeight.toFloat())
        }
        gestureBuilder.addStroke(StrokeDescription(path, 0, 50L))
        gestureBuilder.build()
    }

    private val swipeDownGesture by lazy {
        val gestureBuilder = GestureDescription.Builder()
        val quarterHeight = height * 0.45
        val threeQuarterHeight = height * 0.55
        val halfWidth = width * 0.5
        val path = Path().apply {
            moveTo(halfWidth.toFloat(), quarterHeight.toFloat())
            lineTo(halfWidth.toFloat(), threeQuarterHeight.toFloat())
        }
        gestureBuilder.addStroke(StrokeDescription(path, 0, 50L))
        gestureBuilder.build()
    }

    private val swipeLeftGesture by lazy {
        val gestureBuilder = GestureDescription.Builder()
        val quarterWidth = width * 0.25
        val threeQuarterWidth = width * 0.75
        val halfHeight = height * 0.5
        val path = Path().apply {
            moveTo(threeQuarterWidth.toFloat(), halfHeight.toFloat())
            lineTo(quarterWidth.toFloat(), halfHeight.toFloat())
        }
        gestureBuilder.addStroke(StrokeDescription(path, 0, 50L))
        gestureBuilder.build()
    }

    private val swipeRightGesture by lazy {
        val gestureBuilder = GestureDescription.Builder()
        val quarterWidth = width * 0.25
        val threeQuarterWidth = width * 0.75
        val halfHeight = height * 0.5
        val path = Path().apply {
            moveTo(quarterWidth.toFloat(), halfHeight.toFloat())
            lineTo(threeQuarterWidth.toFloat(), halfHeight.toFloat())
        }
        gestureBuilder.addStroke(StrokeDescription(path, 0, 50L))
        gestureBuilder.build()
    }

    override fun onTrigger() {
        gestureAccessibilityService.rootInActiveWindow?.run {
            val gesture = when(swipeDirection) {
                SwipeDirection.UP -> swipeUpGesture
                SwipeDirection.DOWN -> swipeDownGesture
                SwipeDirection.LEFT -> swipeLeftGesture
                SwipeDirection.RIGHT -> swipeRightGesture
            }
            gestureAccessibilityService.dispatchGesture(gesture, null, null)
        }
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

    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }

}