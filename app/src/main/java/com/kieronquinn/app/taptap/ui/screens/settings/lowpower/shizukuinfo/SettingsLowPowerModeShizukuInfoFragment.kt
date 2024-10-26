package com.kieronquinn.app.taptap.ui.screens.settings.lowpower.shizukuinfo

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentSettingsLowPowerModeShizukuInfoBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsLowPowerModeShizukuInfoFragment: BoundFragment<FragmentSettingsLowPowerModeShizukuInfoBinding>(FragmentSettingsLowPowerModeShizukuInfoBinding::inflate), BackAvailable {

    private val viewModel by viewModel<SettingsLowPowerModeShizukuInfoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupShizukuButton()
        setupSuiButton()
        setupScrollView()
    }

    private fun setupMonet() {
        binding.settingsLowPowerModeShizukuInfoErrorCardSui.applyBackgroundTint(monet)
        binding.settingsLowPowerModeShizukuInfoErrorCardShizuku.applyBackgroundTint(monet)
    }

    private fun setupShizukuButton() = whenResumed {
        binding.settingsLowPowerModeShizukuInfoErrorCardShizukuButton.onClicked().collect {
            viewModel.onShizukuClicked(requireContext())
        }
    }

    private fun setupScrollView() = with(binding.settingsLowPowerModeShizukuInfoError) {
        applyBottomInsets(binding.root)
    }

    private fun setupSuiButton() = whenResumed {
        binding.settingsLowPowerModeShizukuInfoErrorCardSuiButton.onClicked().collect {
            viewModel.onSuiClicked(requireContext())
        }
    }

}