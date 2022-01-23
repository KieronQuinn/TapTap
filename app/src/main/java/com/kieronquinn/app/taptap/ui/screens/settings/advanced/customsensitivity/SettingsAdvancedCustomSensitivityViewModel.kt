package com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.columbus.sensors.TapTapGestureSensorImpl
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import com.kieronquinn.app.taptap.utils.extensions.ContextHub_hasColumbusNanoApp
import com.kieronquinn.app.taptap.utils.extensions.deviceHasContextHub
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsAdvancedCustomSensitivityViewModel : GenericSettingsViewModel() {

    abstract val customSensitivity: StateFlow<String>
    abstract val isSensitivityValid: Flow<Boolean>

    abstract fun setValue(value: String)
    abstract fun onPositiveClicked()
    abstract fun onNegativeClicked()
    abstract fun onNeutralClicked()

}

class SettingsAdvancedCustomSensitivityViewModelImpl(
    context: Context,
    private val settings: TapTapSettings,
    private val navigation: ContainerNavigation
) : SettingsAdvancedCustomSensitivityViewModel() {

    override val restartService = MutableSharedFlow<Unit>()

    private val isLowPowerModeInUse =
        context.deviceHasContextHub && ContextHub_hasColumbusNanoApp() && settings.lowPowerMode.getSync()

    private val rawSetting = settings.columbusCustomSensitivity

    private fun getSettingValue(): Float {
        val customSetting = rawSetting.getSync()
        if(customSetting != -1f) return customSetting
        val level = settings.columbusSensitivityLevel.getSync()
        return TapTapGestureSensorImpl.SENSITIVITY_VALUES.getOrNull(level)
            ?: TapTapGestureSensorImpl.SENSITIVITY_VALUES[TapTapSettingsImpl.DEFAULT_COLUMBUS_SENSITIVITY_LEVEL]
    }

    private val _customSensitivity = MutableStateFlow(getSettingValue().toString())
    override val customSensitivity = _customSensitivity.asStateFlow()
    override val isSensitivityValid = _customSensitivity.map { it.toFloatOrNull() != null }

    override fun setValue(value: String) {
        viewModelScope.launch {
            _customSensitivity.emit(value)
        }
    }

    override fun onPositiveClicked() {
        viewModelScope.launch {
            val value = _customSensitivity.value.toFloatOrNull() ?: return@launch
            rawSetting.set(value)
            restartService.emit(Unit)
            navigation.navigateBack()
        }
    }

    override fun onNegativeClicked() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onNeutralClicked() {
        viewModelScope.launch {
            rawSetting.clear()
            restartService.emit(Unit)
            navigation.navigateBack()
        }
    }

}
