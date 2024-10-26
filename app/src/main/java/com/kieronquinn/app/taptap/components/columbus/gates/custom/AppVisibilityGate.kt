package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.inject

class AppVisibilityGate(
    serviceLifecycle: Lifecycle,
    context: Context,
    private val packageName: String
) : TapTapGate(serviceLifecycle, context) {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val appOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName == packageName }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycle.whenCreated {
            appOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        showAccessibilityNotificationIfNeeded()
        return appOpen.value
    }

}