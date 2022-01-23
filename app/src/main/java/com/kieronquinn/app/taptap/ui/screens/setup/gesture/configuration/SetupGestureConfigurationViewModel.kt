package com.kieronquinn.app.taptap.ui.screens.setup.gesture.configuration

import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.GestureConfigurationNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.launch

abstract class SetupGestureConfigurationViewModel: GenericSettingsViewModel() {

    abstract val sensitivitySetting: TapTapSettings.TapTapSetting<Int>
    abstract val isCustomSensitivitySet: Boolean
    abstract val selectedModel: Int

    abstract fun onDeviceSizeClicked()

}

class SetupGestureConfigurationViewModelImpl(private val navigation: GestureConfigurationNavigation, private val settings: TapTapSettings): SetupGestureConfigurationViewModel() {

    override val sensitivitySetting = settings.columbusSensitivityLevel
    override val isCustomSensitivitySet = settings.columbusCustomSensitivity.existsSync()
    override val restartService = restartServiceCombine(settings.columbusSensitivityLevel)

    override val selectedModel: Int
        get() = settings.columbusTapModel.getSync().nameRes

    override fun onDeviceSizeClicked() {
        viewModelScope.launch {
            navigation.navigate(SetupGestureConfigurationFragmentDirections.actionSetupGestureConfigurationFragmentToSetupGestureModelPicker())
        }
    }

}