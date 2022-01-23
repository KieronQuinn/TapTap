package com.kieronquinn.app.taptap.components.accessibility

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.ui.activities.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

interface TapTapAccessibilityRouter {

    sealed class AccessibilityInput {
        data class PerformGlobalAction(val globalActionId: Int): AccessibilityInput()
        abstract class GestureInput: AccessibilityInput()
        data class PerformSwipe(val direction: Direction): GestureInput() {
            enum class Direction {
                UP, DOWN, LEFT, RIGHT
            }
        }
        object PerformHamburgerClick: GestureInput()
    }

    sealed class AccessibilityOutput {
        data class AppOpen(val packageName: String): AccessibilityOutput()
        data class NotificationShadeState(val open: Boolean): AccessibilityOutput()
        data class QuickSettingsShadeState(val open: Boolean): AccessibilityOutput()
    }

    val accessibilityInputBus: Flow<AccessibilityInput>
    val accessibilityOutputBus: Flow<AccessibilityOutput>

    val accessibilityStartBus: Flow<Unit>
    val gestureAccessibilityStartBus: Flow<Unit>

    suspend fun postInput(accessibilityInput: AccessibilityInput)
    suspend fun postOutput(accessibilityOutput: AccessibilityOutput)
    suspend fun onAccessibilityStarted()
    suspend fun onGestureAccessibilityStarted()

    fun bringToFrontOnAccessibilityStart(fragment: Fragment) {
        fragment.viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            this@TapTapAccessibilityRouter.accessibilityStartBus.collect {
                fragment.bringToFront()
            }
        }
    }

    fun bringToFrontOnGestureAccessibilityStart(fragment: Fragment) {
        fragment.viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            this@TapTapAccessibilityRouter.gestureAccessibilityStartBus.collect {
                fragment.bringToFront()
            }
        }
    }

    private fun Fragment.bringToFront() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
    }

}

class TapTapAccessibilityRouterImpl: TapTapAccessibilityRouter {

    override val accessibilityInputBus = MutableSharedFlow<TapTapAccessibilityRouter.AccessibilityInput>()
    override val accessibilityOutputBus = MutableSharedFlow<TapTapAccessibilityRouter.AccessibilityOutput>()
    override val accessibilityStartBus = MutableSharedFlow<Unit>()
    override val gestureAccessibilityStartBus = MutableSharedFlow<Unit>()

    override suspend fun postInput(accessibilityInput: TapTapAccessibilityRouter.AccessibilityInput) {
        accessibilityInputBus.emit(accessibilityInput)
    }

    override suspend fun postOutput(accessibilityOutput: TapTapAccessibilityRouter.AccessibilityOutput) {
        accessibilityOutputBus.emit(accessibilityOutput)
    }

    override suspend fun onAccessibilityStarted() {
        accessibilityStartBus.emit(Unit)
    }

    override suspend fun onGestureAccessibilityStarted() {
        gestureAccessibilityStartBus.emit(Unit)
    }

}