package com.kieronquinn.app.taptap.ui.screens.settings.battery

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsBatteryFragment : GenericSettingsFragment(), BackAvailable {

    private val adapter by lazy {
        SettingsBatteryAdapter()
    }

    override val viewModel by viewModel<SettingsBatteryViewModel>()

    override val items by lazy {
        listOf(
            SettingsItem.Info(
                contentRes = R.string.settings_battery_header,
                linkClicked = viewModel::onHeaderLinkClicked,
                icon = R.drawable.ic_warning,
                onDismissClicked = viewModel::onHeaderDismissed,
                isVisible = { viewModel.showHeader.value }
            ),
            SettingsItem.Info(
                contentRes = R.string.settings_battery_info,
                linkClicked = viewModel::onDontKillLinkClicked
            ),
            SettingsItem.Switch(
                titleRes = R.string.settings_battery_disable_optimisation,
                content = {
                    if (viewModel.batteryOptimisationDisabled.value) {
                        getString(R.string.settings_battery_disable_optimisation_desc_disabled)
                    } else {
                        getString(R.string.settings_battery_disable_optimisation_desc_enabled)
                    }
                },
                isEnabled = { !viewModel.batteryOptimisationDisabled.value },
                setting = viewModel.batteryOptimisationSetting,
                icon = R.drawable.ic_settings_battery_android
            ),
            SettingsItem.Info(
                isVisible = { viewModel.oemBatteryAvailable },
                contentRes = R.string.settings_battery_oem_info
            ),
            SettingsItem.Text(
                titleRes = R.string.settings_battery_oem_button,
                isVisible = { viewModel.oemBatteryAvailable },
                icon = R.drawable.ic_settings_battery_oem,
                onClick = { viewModel.onOemClicked(requireContext()) }
            )
        )
    }

    override fun createAdapter(items: List<SettingsItem>): GenericSettingsAdapter {
        return adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderDismiss()
    }

    private fun setupHeaderDismiss() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.showHeader.collect {
            adapter.refreshVisibleItems()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        adapter.refreshVisibleItems()
    }

    inner class SettingsBatteryAdapter :
        GenericSettingsAdapter(requireContext(), binding.settingsGenericRecyclerView, items)

}