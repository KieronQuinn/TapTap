package com.kieronquinn.app.taptap.service.accessibility

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.utils.extensions.getStaticStatusBarHeight
import com.kieronquinn.app.taptap.utils.extensions.px
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.app.taptap.utils.lifecycle.LifecycleAccessibilityService
import kotlinx.coroutines.flow.filter
import org.koin.android.ext.android.inject

class TapTapGestureAccessibilityService : LifecycleAccessibilityService() {

    private val router by inject<TapTapAccessibilityRouter>()

    private val displayMetrics by lazy {
        resources.displayMetrics
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
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50L))
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
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50L))
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
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50L))
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
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50L))
        gestureBuilder.build()
    }

    override fun onCreate() {
        super.onCreate()
        lifecycle.whenCreated {
            setupInputListener()
        }
    }

    private suspend fun setupInputListener() {
        router.accessibilityInputBus.filter {
            it is TapTapAccessibilityRouter.AccessibilityInput.GestureInput
        }.collect {
            handleInput(it as TapTapAccessibilityRouter.AccessibilityInput.GestureInput)
        }
    }

    private fun handleInput(accessibilityInput: TapTapAccessibilityRouter.AccessibilityInput.GestureInput) {
        when(accessibilityInput) {
            is TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe -> {
                performSwipe(accessibilityInput.direction)
            }
            is TapTapAccessibilityRouter.AccessibilityInput.PerformHamburgerClick -> {
                performHamburgerClick()
            }
            is TapTapAccessibilityRouter.AccessibilityInput.PerformSingleTouch -> {
                performSingleTouch()
            }
        }
    }

    private fun performSwipe(direction: TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction) {
        rootInActiveWindow?.run {
            val gesture = when (direction) {
                TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction.UP -> swipeUpGesture
                TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction.DOWN -> swipeDownGesture
                TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction.LEFT -> swipeLeftGesture
                TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction.RIGHT -> swipeRightGesture
            }
            dispatchGesture(gesture, null, null)
        }
    }

    private fun performHamburgerClick() {
        rootInActiveWindow?.run {
            val gesture = createClick(25f.px, getStaticStatusBarHeight(
                this@TapTapGestureAccessibilityService
            ) + 25f.px)
            dispatchGesture(gesture, null, null)
        }
    }

    private fun performSingleTouch() {
        rootInActiveWindow?.run {
            val gesture = createClick(100f.px, 100f.px)
            dispatchGesture(gesture, null, null)
        }
    }

    private fun createClick(x: Float, y: Float): GestureDescription {
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, 1L)
        val clickBuilder = GestureDescription.Builder()
        clickBuilder.addStroke(clickStroke)
        return clickBuilder.build()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //no-op
    }

    override fun onInterrupt() {
        //no-op
    }

}