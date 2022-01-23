package com.kieronquinn.app.taptap.ui.screens.reachability

import android.accessibilityservice.AccessibilityService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import com.kieronquinn.app.taptap.components.settings.invert
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class ReachabilityViewModel : ViewModel() {

    abstract suspend fun getHasLeftHandedSet(): Boolean
    abstract val isLeftHanded: Flow<Boolean>
    abstract fun sendStartAction()
    abstract fun onNotificationsClicked()
    abstract fun onQuickSettingsClicked()
    abstract fun onNotificationsLongClicked()
    abstract fun getCurrentApp(): String?

}

class ReachabilityViewModelImpl(
    private val settings: TapTapSettings,
    private val accessibilityRouter: TapTapAccessibilityRouter
) : ReachabilityViewModel() {

    private var currentApp: StateFlow<String>? = null

    init {
        viewModelScope.launch {
            currentApp = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
                .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName }
                .stateIn(viewModelScope)
        }
    }

    override suspend fun getHasLeftHandedSet(): Boolean {
        val hasKey = settings.reachabilityLeftHanded.exists()
        settings.reachabilityLeftHanded.set(TapTapSettingsImpl.DEFAULT_REACHABILITY_LEFT_HANDED)
        return hasKey
    }

    override val isLeftHanded = settings.reachabilityLeftHanded.asFlow()

    private var hasSentStartAction = false
    override fun sendStartAction() {
        if(hasSentStartAction) return
        hasSentStartAction = true
        viewModelScope.launch {
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
                )
            )
        }
    }

    override fun onNotificationsClicked() {
        viewModelScope.launch {
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
                )
            )
        }
    }

    override fun onQuickSettingsClicked() {
        viewModelScope.launch {
            accessibilityRouter.postInput(
                TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
                )
            )
        }
    }

    override fun onNotificationsLongClicked() {
        viewModelScope.launch {
            settings.reachabilityLeftHanded.invert()
        }
    }

    override fun getCurrentApp(): String? {
        return currentApp?.value
    }

}

