package com.kieronquinn.app.taptap.ui.screens.settings.lowpower

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kieronquinn.app.taptap.databinding.FragmentSettingsLowPowerModeBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.views.MonetSwitch
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsLowPowerModeFragment: BoundFragment<FragmentSettingsLowPowerModeBinding>(FragmentSettingsLowPowerModeBinding::inflate), BackAvailable {

    private val viewModel by viewModel<SettingsLowPowerModeViewModel>()
    private val containerViewModel by sharedViewModel<ContainerSharedViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwitch()
        setupSwitchEnabled()
        setupSwitchChecked()
        setupShizukuButton()
        setupShizuku()
        setupSuiButton()
        setupCompatible()
        setupRestart()
        setupScrollView()
    }


    private fun setupSwitch() = whenResumed {
        binding.settingsLowPowerModeEnable.onClicked().collect {
            viewModel.onLowPowerSwitchClicked(it as MonetSwitch)
        }
    }

    private fun setupSwitchChecked() {
        binding.settingsLowPowerModeEnable.isChecked = viewModel.lowPowerModeInitialValue
        whenResumed {
            viewModel.lowPowerModeEnabled.collect {
                binding.settingsLowPowerModeEnable.isChecked = it
            }
        }
    }

    private fun setupSwitchEnabled() = whenResumed {
        viewModel.switchEnabled.collect {
            binding.settingsLowPowerModeEnable.isEnabled = it
            binding.settingsLowPowerModeEnable.alpha = if(it) 1f else 0.5f
        }
    }

    private fun setupShizuku() = whenResumed {
        viewModel.shizukuInstalled.collect {
            binding.settingsLowPowerModeErrorShizuku.isVisible = !it
        }
    }

    private fun setupCompatible() = whenResumed {
        viewModel.lowPowerModeCompatible.collect {
            binding.settingsLowPowerModeErrorIncompatible.isVisible = !it
        }
    }

    private fun setupRestart() = whenResumed {
        viewModel.restartService?.collect {
            containerViewModel.restartService(requireContext())
        }
    }

    private fun setupShizukuButton() = whenResumed {
        binding.settingsLowPowerModeErrorButtonShizuku.onClicked().collect {
            viewModel.onShizukuClicked(requireContext())
        }
    }

    private fun setupSuiButton() = whenResumed {
        binding.settingsLowPowerModeErrorButtonSui.onClicked().collect {
            viewModel.onSuiClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShizukuState(requireContext())
    }

    private fun setupScrollView() = with(binding.settingsLowPowerModeScrollView) {
        applyBottomInsets(binding.root)
    }

}