package com.kieronquinn.app.taptap.ui.screens.settings.advanced

import android.os.Build
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsAdvancedFragment: GenericSettingsFragment(), BackAvailable {

    override val viewModel by viewModel<SettingsAdvancedViewModel>()

    override val items by lazy {
        listOf(
            SettingsItem.Info(
                contentRes = R.string.setting_advanced_warning_desc
            ),
            SettingsItem.Switch(
                icon = R.drawable.ic_setting_low_power_mode,
                titleRes = R.string.setting_advanced_tensor_low_power,
                contentRes = R.string.setting_advanced_tensor_low_power_desc,
                setting = viewModel.tensorLowPowerMode,
                isVisible = { !viewModel.isLowPowerModeEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 }
            ),
            SettingsItem.Switch(
                icon = R.drawable.ic_settings_advanced_auto_restart,
                titleRes = R.string.setting_advanced_restart_service,
                contentRes = R.string.setting_advanced_restart_service_desc,
                isVisible = { !viewModel.isLowPowerModeEnabled },
                setting = viewModel.autoRestartServiceSetting
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_gesture_sensitivity,
                titleRes = R.string.setting_advanced_custom_sensitivity,
                contentRes = R.string.setting_advanced_custom_sensitivity_desc,
                isVisible = { !viewModel.isLowPowerModeEnabled },
                onClick = viewModel::onCustomSensitivityClicked
            ),
            SettingsItem.Switch(
                icon = R.drawable.ic_feedback_wake_device,
                titleRes = R.string.setting_advanced_legacy_wake,
                contentRes = R.string.setting_advanced_legacy_wake_desc,
                setting = viewModel.legacyWakeSetting
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_settings_advanced_monet,
                titleRes = R.string.settings_advanced_monet_color_picker,
                contentRes = R.string.settings_advanced_monet_color_picker_desc,
                onClick = viewModel::onMonetClicked,
                isVisible = { Build.VERSION.SDK_INT < Build.VERSION_CODES.S }
            )
        )
    }

    override fun createAdapter(items: List<SettingsItem>): GenericSettingsAdapter {
        return SettingsAdvancedAdapter()
    }

    inner class SettingsAdvancedAdapter: GenericSettingsAdapter(requireContext(), binding.settingsGenericRecyclerView, items)

}