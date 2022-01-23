package com.kieronquinn.app.taptap.ui.screens.settings.advanced

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import com.kieronquinn.app.taptap.utils.extensions.ContextHub_hasColumbusNanoApp
import com.kieronquinn.app.taptap.utils.extensions.deviceHasContextHub
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class SettingsAdvancedViewModel: GenericSettingsViewModel() {

    abstract val autoRestartServiceSetting: TapTapSettings.TapTapSetting<Boolean>
    abstract val isLowPowerModeEnabled: Boolean
    abstract val legacyWakeSetting: TapTapSettings.TapTapSetting<Boolean>
    abstract val tensorLowPowerMode: TapTapSettings.TapTapSetting<Boolean>

    abstract fun onCustomSensitivityClicked()
    abstract fun onMonetClicked()

}

class SettingsAdvancedViewModelImpl(settings: TapTapSettings, context: Context, private val navigation: ContainerNavigation): SettingsAdvancedViewModel() {

    override val autoRestartServiceSetting = settings.advancedAutoRestart
    override val legacyWakeSetting = settings.advancedLegacyWake
    override val tensorLowPowerMode = settings.advancedTensorLowPower

    override val restartService = restartServiceCombine(autoRestartServiceSetting)

    private val isLowPowerModeSupported = context.deviceHasContextHub && ContextHub_hasColumbusNanoApp()
    override val isLowPowerModeEnabled = isLowPowerModeSupported && settings.lowPowerMode.getSync()

    override fun onCustomSensitivityClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsAdvancedFragmentDirections.actionSettingsAdvancedFragmentToSettingsAdvancedCustomSensitivityFragment())
        }
    }

    override fun onMonetClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsAdvancedFragmentDirections.actionSettingsAdvancedFragmentToSettingsWallpaperColorPickerBottomSheetFragment())
        }
    }

}