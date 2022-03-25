package com.kieronquinn.app.taptap.ui.screens.settings.options

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import com.kieronquinn.app.taptap.utils.extensions.canUseContextHub
import com.kieronquinn.app.taptap.utils.extensions.isNativeColumbusEnabled
import kotlinx.coroutines.launch

abstract class SettingsOptionsViewModel: GenericSettingsViewModel() {

    abstract val isLowPowerModeSupported: Boolean
    abstract val isLowPowerModeEnabled: Boolean
    abstract val isLowPowerModeAvailable: Boolean
    abstract val isNativeModeAvailable: Boolean
    abstract val isNativeModeEnabled: Boolean
    abstract val isCustomSensitivitySet: Boolean
    abstract val sensitivitySettingCHRE: TapTapSettings.TapTapSetting<Boolean>
    abstract val sensitivitySetting: TapTapSettings.TapTapSetting<Int>

    abstract fun onLowPowerModeClicked()
    abstract fun onNativeModeClicked()
    abstract fun onAdvancedClicked()
    abstract fun onModelClicked()

}

class SettingsOptionsViewModelImpl(private val settings: TapTapSettings, context: Context, private val navigation: ContainerNavigation): SettingsOptionsViewModel() {

    private val getIsNativeModeEnabled = { context.isNativeColumbusEnabled() }

    override val isLowPowerModeSupported = context.canUseContextHub
    //These get checked in onResume so needs to be up-to-date
    override val isLowPowerModeEnabled
        get() = isLowPowerModeSupported && settings.lowPowerMode.getSync()
    override val isCustomSensitivitySet
        get() = settings.columbusCustomSensitivity.existsSync()
    override val isNativeModeAvailable
        get() = !isLowPowerModeEnabled
    override val isLowPowerModeAvailable
        get() = !getIsNativeModeEnabled()
    override val isNativeModeEnabled
        get() = getIsNativeModeEnabled()
    override val sensitivitySettingCHRE = settings.columbusCHRELowSensitivity
    override val sensitivitySetting = settings.columbusSensitivityLevel
    override val restartService = restartServiceCombine(sensitivitySetting)

    override fun onLowPowerModeClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsOptionsFragmentDirections.actionSettingsOptionsFragmentToSettingsLowPowerModeFragment())
        }
    }

    override fun onNativeModeClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsOptionsFragmentDirections.actionSettingsOptionsFragmentToSettingsNativeModeFragment())
        }
    }

    override fun onAdvancedClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsOptionsFragmentDirections.actionSettingsOptionsFragmentToSettingsAdvancedFragment())
        }
    }

    override fun onModelClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsOptionsFragmentDirections.actionSettingsOptionsFragmentToSettingsModelPickerFragment())
        }
    }

}