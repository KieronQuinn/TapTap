package com.kieronquinn.app.taptap.ui.screens.settings.lowpower

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.invert
import com.kieronquinn.app.taptap.components.sui.SuiProvider
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import com.kieronquinn.app.taptap.ui.views.MonetSwitch
import com.kieronquinn.app.taptap.utils.extensions.ContextHub_hasColumbusNanoApp
import com.kieronquinn.app.taptap.utils.extensions.Shizuku_requestPermissionIfNeeded
import com.kieronquinn.app.taptap.utils.extensions.deviceHasContextHub
import com.kieronquinn.app.taptap.utils.extensions.isPackageInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.ShizukuProvider

abstract class SettingsLowPowerModeViewModel: GenericSettingsViewModel() {

    abstract val lowPowerModeInitialValue: Boolean
    abstract val lowPowerModeEnabled: Flow<Boolean>
    abstract val lowPowerModeCompatible: Flow<Boolean>
    abstract val shizukuInstalled: Flow<Boolean>
    abstract val switchEnabled: Flow<Boolean>

    abstract fun checkShizukuState(context: Context)
    abstract fun onLowPowerSwitchClicked(switch: MonetSwitch)
    abstract fun onShizukuClicked(context: Context)
    abstract fun onSuiClicked()

}

class SettingsLowPowerModelViewModelImpl(settings: TapTapSettings, context: Context, private val navigation: ContainerNavigation, private val suiProvider: SuiProvider): SettingsLowPowerModeViewModel() {

    private val lowPowerModeSetting = settings.lowPowerMode
    private val hasPreviouslyGrantedSui = settings.hasPreviouslyGrantedSui

    override val lowPowerModeInitialValue = lowPowerModeSetting.getSync()
    override val restartService = restartServiceCombine(lowPowerModeSetting)

    override val lowPowerModeEnabled = lowPowerModeSetting.asFlow()
    override val lowPowerModeCompatible = flow {
        emit(context.deviceHasContextHub && ContextHub_hasColumbusNanoApp())
    }

    private val suiGranted = MutableStateFlow(false)

    private val _shizukuInstalled = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val shizukuInstalled = combine(_shizukuInstalled, lowPowerModeCompatible, suiGranted) { shizuku, lowPower, sui ->
        //We shouldn't show the Shizuku warning when the device isn't compatible, or if sui is granted
        shizuku || !lowPower || sui
    }

    override val switchEnabled = combine(lowPowerModeCompatible, _shizukuInstalled, suiGranted) { lowPower, shizuku, sui ->
        lowPower && (shizuku || sui)
    }

    override fun checkShizukuState(context: Context) {
        viewModelScope.launch {
            if(hasPreviouslyGrantedSui.get()){
                checkSui()
            }
            _shizukuInstalled.emit(context.isPackageInstalled(ShizukuProvider.MANAGER_APPLICATION_ID))
        }
    }

    override fun onLowPowerSwitchClicked(switch: MonetSwitch) {
        viewModelScope.launch {
            if(!lowPowerModeSetting.get()) {
                //Check permission first
                val permissionGranted = checkPermissions()
                if(permissionGranted == null){
                    //Shizuku is not running
                    Toast.makeText(switch.context, R.string.settings_low_power_mode_error_toast, Toast.LENGTH_LONG).show()
                }
                if(permissionGranted != true){
                    lowPowerModeSetting.set(false)
                    switch.isChecked = false
                    return@launch
                }
            }
            lowPowerModeSetting.invert()
        }
    }

    private suspend fun checkPermissions() = withContext(Dispatchers.IO) {
        return@withContext Shizuku_requestPermissionIfNeeded()
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
            navigation.navigate(SettingsLowPowerModeFragmentDirections.actionSettingsLowPowerModeFragmentToSettingsLowPowerModeShizukuInfoFragment())
        }
    }

    override fun onSuiClicked() {
        viewModelScope.launch {
            checkSui()
        }
    }

}