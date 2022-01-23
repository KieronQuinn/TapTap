package com.kieronquinn.app.taptap.ui.screens.settings.feedback

import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFeedbackFragment: GenericSettingsFragment(), BackAvailable {

    override val viewModel by viewModel<SettingsFeedbackViewModel>()

    override val items by lazy {
        listOf(
            GenericSettingsViewModel.SettingsItem.Switch(
                icon = R.drawable.ic_feedback,
                titleRes = R.string.setting_feedback_vibrate,
                contentRes = R.string.setting_feedback_vibrate_desc,
                setting = viewModel.vibrateSetting
            ),
            GenericSettingsViewModel.SettingsItem.Switch(
                icon = R.drawable.ic_feedback_dnd,
                titleRes = R.string.setting_feedback_override_dnd,
                contentRes = R.string.setting_feedback_override_dnd_desc,
                setting = viewModel.vibrateDNDSetting
            ),
            GenericSettingsViewModel.SettingsItem.Switch(
                icon = R.drawable.ic_feedback_wake_device,
                titleRes = R.string.setting_feedback_wake,
                contentRes = R.string.setting_feedback_wake_desc,
                setting = viewModel.wakeDeviceSetting
            )
        )
    }

    override fun createAdapter(items: List<GenericSettingsViewModel.SettingsItem>): GenericSettingsAdapter {
        return SettingsFeedbackAdapter()
    }

    inner class SettingsFeedbackAdapter: GenericSettingsAdapter(requireContext(), binding.settingsGenericRecyclerView, items)

}