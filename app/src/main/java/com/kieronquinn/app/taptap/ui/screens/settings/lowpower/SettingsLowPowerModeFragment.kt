package com.kieronquinn.app.taptap.ui.screens.settings.lowpower

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsLowPowerModeBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.view.MonetSwitch
import kotlinx.coroutines.flow.collect
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


    private fun setupSwitch() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsLowPowerModeEnable.onClicked().collect {
            viewModel.onLowPowerSwitchClicked(it as MonetSwitch)
        }
    }

    private fun setupSwitchChecked() {
        binding.settingsLowPowerModeEnable.isChecked = viewModel.lowPowerModeInitialValue
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.lowPowerModeEnabled.collect {
                binding.settingsLowPowerModeEnable.isChecked = it
            }
        }
    }

    private fun setupSwitchEnabled() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.switchEnabled.collect {
            binding.settingsLowPowerModeEnable.isEnabled = it
            binding.settingsLowPowerModeEnable.alpha = if(it) 1f else 0.5f
        }
    }

    private fun setupShizuku() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.shizukuInstalled.collect {
            binding.settingsLowPowerModeErrorShizuku.isVisible = !it
        }
    }

    private fun setupCompatible() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.lowPowerModeCompatible.collect {
            binding.settingsLowPowerModeErrorIncompatible.isVisible = !it
        }
    }

    private fun setupRestart() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.restartService?.collect {
            containerViewModel.restartService(requireContext())
        }
    }

    private fun setupShizukuButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsLowPowerModeErrorButtonShizuku.onClicked().collect {
            viewModel.onShizukuClicked(requireContext())
        }
    }

    private fun setupSuiButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
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