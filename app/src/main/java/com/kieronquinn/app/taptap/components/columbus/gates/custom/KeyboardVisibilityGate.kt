package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.getSettingAsFlow
import com.kieronquinn.app.taptap.utils.extensions.isKeyboardOpen
import com.kieronquinn.app.taptap.utils.extensions.secureStringConverter
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject

class KeyboardVisibilityGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val keyboardUri = Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD)
    private val appOpen = accessibilityRouter.accessibilityOutputBus
        .filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName }

    private val keyboardPackageName = context.getSettingAsFlow(
        keyboardUri,
        context.secureStringConverter(Settings.Secure.DEFAULT_INPUT_METHOD)
    )

    private var keyboardOpen = combine(appOpen, keyboardPackageName) { app, keyboard ->
        return@combine (keyboard != null && app == keyboard)
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycleScope.launchWhenCreated {
            keyboardOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        showAccessibilityNotificationIfNeeded()
        return context.isKeyboardOpen() || keyboardOpen.value
    }

}