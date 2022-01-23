package com.kieronquinn.app.taptap.ui.screens.settings.feedback

import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel

abstract class SettingsFeedbackViewModel : GenericSettingsViewModel() {

    abstract val vibrateSetting: TapTapSettings.TapTapSetting<Boolean>
    abstract val vibrateDNDSetting: TapTapSettings.TapTapSetting<Boolean>
    abstract val wakeDeviceSetting: TapTapSettings.TapTapSetting<Boolean>

}

class SettingsFeedbackViewModelImpl(settings: TapTapSettings) : SettingsFeedbackViewModel() {

    override val vibrateSetting by settings::feedbackVibrate
    override val vibrateDNDSetting by settings::feedbackVibrateDND
    override val wakeDeviceSetting by settings::feedbackWakeDevice
    override val restartService = restartServiceCombine(vibrateSetting, wakeDeviceSetting) //vibrateDND doesn't need a restart

}