package com.kieronquinn.app.taptap.ui.screens.settings.shared.internet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class SettingsSharedInternetPermissionDialogViewModel: ViewModel() {

    abstract val dismissBus: Flow<Unit>

    abstract fun onAllowAlwaysClicked()
    abstract fun onAllowRunningClicked()
    abstract fun onDenyClicked()

}

class SettingsSharedInternetPermissionDialogViewModelImpl(
    settings: TapTapSettings
): SettingsSharedInternetPermissionDialogViewModel() {

    private val internetAllowed = settings.internetAllowed
    private val backgroundUpdateCheck = settings.backgroundUpdateCheck

    override val dismissBus = MutableSharedFlow<Unit>()

    //Allow always = internet allowed & background update check allowed
    override fun onAllowAlwaysClicked() {
        viewModelScope.launch {
            internetAllowed.set(true)
            backgroundUpdateCheck.set(true)
            dismissBus.emit(Unit)
        }
    }

    //Allow while running = internet allowed but background update check not allowed (it will check on launch)
    override fun onAllowRunningClicked() {
        viewModelScope.launch {
            internetAllowed.set(true)
            backgroundUpdateCheck.set(false)
            dismissBus.emit(Unit)
        }
    }

    //Deny = internet and background checks not allowed
    override fun onDenyClicked() {
        viewModelScope.launch {
            internetAllowed.set(false)
            backgroundUpdateCheck.set(false)
            dismissBus.emit(Unit)
        }
    }

}