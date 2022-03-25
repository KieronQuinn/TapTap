package com.kieronquinn.app.taptap.ui.screens.settings.nativemode

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.utils.extensions.doesHaveLogPermission
import com.kieronquinn.app.taptap.utils.extensions.getColumbusSetupNotificationRequiredFlow
import com.kieronquinn.app.taptap.utils.extensions.isNativeColumbusEnabled
import com.kieronquinn.monetcompat.view.MonetSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope

abstract class SettingsNativeModeViewModel: ViewModel(), KoinScopeComponent, KoinComponent {

    abstract val toastBus: Flow<Int>
    abstract val setupNotificationBus: Flow<Unit>
    abstract val nativeModeEnabled: StateFlow<Boolean>
    abstract val isSetupRequired: StateFlow<Boolean>
    abstract val switchEnabled: Flow<Boolean>
    abstract fun checkState(context: Context)

    abstract fun onManualSetupClicked()
    abstract fun onAutomaticSetupClicked(context: Context)
    abstract fun onSwitchClicked(switch: MonetSwitch)

}

class SettingsNativeModeViewModelImpl(
    context: Context,
    private val navigation: ContainerNavigation,
    private val rootServiceRepository: TapTapRootServiceRepository
): SettingsNativeModeViewModel() {

    companion object {
        private const val PACKAGE_SYSTEM_SETTINGS = "com.android.settings"
        private const val LINK_SETUP = "https://kieronquinn.co.uk/redirect/TapTap/nativesetup"
    }

    private val settingsIntent = context.packageManager.getLaunchIntentForPackage(
        PACKAGE_SYSTEM_SETTINGS
    )

    override val scope by lazy {
        createScope(this)
    }

    override val toastBus = MutableSharedFlow<Int>()
    override val nativeModeEnabled = MutableStateFlow(context.isNativeColumbusEnabled())
    override val isSetupRequired = MutableStateFlow(!context.doesHaveLogPermission())
    override val switchEnabled = isSetupRequired.map { !it }
    override val setupNotificationBus = context.getColumbusSetupNotificationRequiredFlow()

    private val shizukuServiceRepository by scope.inject<TapTapShizukuServiceRepository>()

    override fun onCleared() {
        super.onCleared()
        scope.close()
    }

    override fun checkState(context: Context) {
        viewModelScope.launch {
            nativeModeEnabled.emit(context.isNativeColumbusEnabled())
            isSetupRequired.emit(!context.doesHaveLogPermission())
        }
    }

    override fun onAutomaticSetupClicked(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            //Give the user a warning as this will kill the app
            toastBus.emit(R.string.settings_native_mode_automatic_warning_toast)
            delay(1500L)
            if(!runAutomaticSetup()){
                toastBus.emit(R.string.settings_native_mode_automatic_toast)
            }
        }
    }

    private suspend fun runAutomaticSetup(): Boolean {
        val shizukuResponse = shizukuServiceRepository.runWithShellService {
            it.grantReadLogsPermission()
        }
        if(shizukuResponse is TapTapShizukuServiceRepository.ShizukuServiceResponse.Success)
            return true
        val suiResponse = shizukuServiceRepository.runWithService {
            it.grantReadLogsPermission()
        }
        if(suiResponse is TapTapShizukuServiceRepository.ShizukuServiceResponse.Success)
            return true
        val rootResponse = rootServiceRepository.runWithService {
            it.grantReadLogsPermission()
        }
        return rootResponse != null
    }

    override fun onManualSetupClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(LINK_SETUP)
            })
        }
    }

    override fun onSwitchClicked(switch: MonetSwitch) {
        switch.isChecked = !switch.isChecked
        viewModelScope.launch {
            navigation.navigate(settingsIntent ?: return@launch)
            if(switch.context.isNativeColumbusEnabled()) {
                toastBus.emit(R.string.settings_native_mode_launch_toast_disable)
            }else{
                toastBus.emit(R.string.settings_native_mode_launch_toast)
            }
        }
    }

}