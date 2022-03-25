package com.kieronquinn.app.taptap.ui.screens.settings.options

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.databinding.FragmentSettingsOptionsBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.CanShowSnackbar
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsOptionsFragment: BoundFragment<FragmentSettingsOptionsBinding>(FragmentSettingsOptionsBinding::inflate), CanShowSnackbar {

    private val viewModel by viewModel<SettingsOptionsViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val containerViewModel by sharedViewModel<ContainerSharedViewModel>()

    private val adapter by lazy {
        SettingsOptionsAdapter()
    }

    private val items by lazy {
        listOf(
            SettingsItem.Text(
                icon = R.drawable.ic_setting_low_power_mode,
                titleRes = R.string.setting_low_power_mode,
                content = { if(viewModel.isLowPowerModeAvailable){
                    getString(R.string.setting_low_power_mode_desc)
                }else{
                    getString(R.string.setting_low_power_mode_desc_unavailable)
                }},
                isEnabled = viewModel::isLowPowerModeAvailable,
                onClick = viewModel::onLowPowerModeClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_settings_native_mode,
                titleRes = R.string.settings_native_mode,
                content = { if(viewModel.isNativeModeAvailable){
                    getString(R.string.settings_native_mode_desc)
                }else{
                    getString(R.string.settings_native_mode_desc_unavailable)
                }},
                isVisible = viewModel::isLowPowerModeSupported,
                isEnabled = viewModel::isNativeModeAvailable,
                onClick = viewModel::onNativeModeClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_settings_device_model,
                titleRes = R.string.setting_gesture_model,
                content = {
                    when {
                        viewModel.isLowPowerModeEnabled -> {
                            getString(R.string.setting_gesture_model_desc_not_available)
                        }
                        viewModel.isNativeModeEnabled -> {
                            getString(R.string.setting_gesture_model_desc_not_available_native)
                        }
                        else -> {
                            getString(R.string.setting_gesture_model_desc)
                        }
                    }
                },
                onClick = viewModel::onModelClicked,
                isEnabled = { !viewModel.isLowPowerModeEnabled && !viewModel.isNativeModeEnabled }
            ),
            SettingsItem.Slider(
                icon = R.drawable.ic_gesture_sensitivity,
                titleRes = R.string.setting_gesture_sensitivity,
                content = { if(viewModel.isCustomSensitivitySet) {
                    getString(R.string.setting_gesture_sensitivity_desc_disabled)
                }else{
                    getString(R.string.setting_gesture_sensitivity_desc)
                 }},
                setting = viewModel.sensitivitySetting as TapTapSettings.TapTapSetting<Number>,
                stepSize = 1.0f,
                isEnabled = { !viewModel.isCustomSensitivitySet },
                isVisible = { !viewModel.isLowPowerModeEnabled && !viewModel.isNativeModeEnabled },
                labelFormatter = {
                    getString(when {
                        it < 2 -> R.string.slider_sensitivity_very_low
                        it < 4 -> R.string.slider_sensitivity_low
                        it < 6 -> R.string.slider_sensitivity_medium
                        it < 8 -> R.string.slider_sensitivity_high
                        else -> R.string.slider_sensitivity_very_high
                    })
                }
            ),
            SettingsItem.Switch(
                icon = R.drawable.ic_gesture_sensitivity_lower,
                titleRes = R.string.setting_gesture_sensitivity_chre,
                content = { getString(R.string.setting_gesture_sensitivity_chre_desc) },
                setting = viewModel.sensitivitySettingCHRE,
                isVisible = { viewModel.isLowPowerModeEnabled && !viewModel.isNativeModeEnabled }
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_settings_advanced,
                titleRes = R.string.setting_advanced,
                contentRes = R.string.setting_advanced_desc,
                onClick = viewModel::onAdvancedClicked
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwitch()
        setupRecyclerView()
        setupRestart()
    }

    private fun setupSwitch(){
        binding.settingsOptionsEnableTapTap.isChecked = sharedViewModel.isServiceRunning.value
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            with(binding.settingsOptionsEnableTapTap) {
                launch {
                    sharedViewModel.isServiceRunning.collect {
                        isChecked = it
                    }
                }
                launch {
                    onClicked().collect {
                        sharedViewModel.toggleServiceEnabledState(it.context)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() = with(binding.settingsOptionsRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsOptionsFragment.adapter
        applyBottomInsets(binding.root)
    }

    private fun setupRestart() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.restartService?.collect {
                containerViewModel.restartService(requireContext())
            }
        }
    }

    override fun onDestroyView() {
        binding.settingsOptionsRecyclerView.adapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        adapter.refreshVisibleItems()
    }

    inner class SettingsOptionsAdapter: GenericSettingsAdapter(requireContext(), binding.settingsOptionsRecyclerView, items)

}