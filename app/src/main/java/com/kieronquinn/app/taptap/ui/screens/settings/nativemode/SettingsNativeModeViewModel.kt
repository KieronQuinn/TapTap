package com.kieronquinn.app.taptap.ui.screens.settings.nativemode

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.sui.SuiProvider
import com.kieronquinn.app.taptap.ui.views.MonetSwitch
import com.kieronquinn.app.taptap.utils.extensions.Shizuku_requestPermissionIfNeeded
import com.kieronquinn.app.taptap.utils.extensions.getColumbusSetupNotificationRequiredFlow
import com.kieronquinn.app.taptap.utils.extensions.isNativeColumbusEnabled
import com.kieronquinn.app.taptap.utils.extensions.isPackageInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import rikka.shizuku.ShizukuProvider

abstract class SettingsNativeModeViewModel: ViewModel(), KoinScopeComponent, KoinComponent {

    abstract val toastBus: Flow<Int>
    abstract val setupNotificationBus: Flow<Unit>
    abstract val nativeModeEnabled: StateFlow<Boolean>
    abstract val shizukuInstalled: Flow<Boolean>
    abstract val switchEnabled: Flow<Boolean>

    abstract fun checkState(context: Context)
    abstract fun onShizukuClicked(context: Context)
    abstract fun onSuiClicked()
    abstract fun onSwitchClicked(switch: MonetSwitch)

}

class SettingsNativeModeViewModelImpl(
    context: Context,
    private val navigation: ContainerNavigation,
    private val suiProvider: SuiProvider,
    settings: TapTapSettings
): SettingsNativeModeViewModel() {

    companion object {
        private const val PACKAGE_SYSTEM_SETTINGS = "com.android.settings"
    }

    private val settingsIntent = context.packageManager.getLaunchIntentForPackage(
        PACKAGE_SYSTEM_SETTINGS
    )

    private val hasPreviouslyGrantedSui = settings.hasPreviouslyGrantedSui

    private val suiGranted = MutableStateFlow(false)

    private val _shizukuInstalled = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val shizukuInstalled = combine(_shizukuInstalled, suiGranted) { shizuku, sui ->
        //We shouldn't show the Shizuku warning if sui is granted
        shizuku || sui
    }

    override val scope by lazy {
        createScope(this)
    }

    override val toastBus = MutableSharedFlow<Int>()
    override val nativeModeEnabled = MutableStateFlow(context.isNativeColumbusEnabled())
    override val switchEnabled = shizukuInstalled
    override val setupNotificationBus = context.getColumbusSetupNotificationRequiredFlow()

    override fun onCleared() {
        super.onCleared()
        scope.close()
    }

    override fun onSwitchClicked(switch: MonetSwitch) {
        switch.isChecked = nativeModeEnabled.value
        viewModelScope.launch {
            if(!nativeModeEnabled.value) {
                //Check permission first
                val permissionGranted = checkPermissions()
                if (permissionGranted == null) {
                    //Shizuku is not running
                    Toast.makeText(
                        switch.context,
                        R.string.settings_low_power_mode_error_toast,
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (permissionGranted != true) {
                    switch.isChecked = false
                    return@launch
                }
            }
            navigation.navigate(settingsIntent ?: return@launch)
            if(switch.context.isNativeColumbusEnabled()) {
                toastBus.emit(R.string.settings_native_mode_launch_toast_disable)
            }else{
                toastBus.emit(R.string.settings_native_mode_launch_toast)
            }
        }
    }

    private suspend fun checkPermissions() = withContext(Dispatchers.IO) {
        return@withContext Shizuku_requestPermissionIfNeeded()
    }

    override fun checkState(context: Context) {
        viewModelScope.launch {
            nativeModeEnabled.emit(context.isNativeColumbusEnabled())
            if(hasPreviouslyGrantedSui.get()){
                checkSui()
            }
            _shizukuInstalled.emit(context.isPackageInstalled(ShizukuProvider.MANAGER_APPLICATION_ID))
        }
    }

    private suspend fun checkSui() = withContext(Dispatchers.IO) {
        if(!suiProvider.isSui) return@withContext false
        //Sui uses the same permission flow as Shizuku, but needs manual checking as we can't check for a manager app
        val sui = Shizuku_requestPermissionIfNeeded() ?: false
        hasPreviouslyGrantedSui.set(sui)
        suiGranted.emit(sui)
    }

    override fun onShizukuClicked(context: Context) {
        viewModelScope.launch {
            navigation.navigate(SettingsNativeModeFragmentDirections.actionSettingsNativeModeFragmentToSettingsLowPowerModeShizukuInfoFragment())
        }
    }

    override fun onSuiClicked() {
        viewModelScope.launch {
            checkSui()
        }
    }

}