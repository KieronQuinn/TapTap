package com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsAdvancedCustomSensitivityBinding
import com.kieronquinn.app.taptap.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsAdvancedCustomSensitivityFragment: BaseBottomSheetFragment<FragmentSettingsAdvancedCustomSensitivityBinding>(FragmentSettingsAdvancedCustomSensitivityBinding::inflate) {

    private val viewModel by viewModel<SettingsAdvancedCustomSensitivityViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()

    private val accent by lazy {
        monet.getAccentColor(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets(view)
        setupValue()
        setupError()
        setupPositive()
        setupNegative()
        setupNeutral()
        setupRestart()
    }

    private fun setupInsets(view: View) {
        binding.root.onApplyInsets { _, insets ->
            val bottomPadding = resources.getDimension(R.dimen.margin_16).toInt()
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = bottomInset + bottomPadding)
        }
    }

    private fun setupValue() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsAdvancedCustomSensitivityInput.applyMonet()
        binding.settingsAdvancedCustomSensitivityEdit.applyMonet()
        viewModel.customSensitivity.take(1).collect {
            binding.settingsAdvancedCustomSensitivityEdit.text?.run {
                clear()
                append(it)
            } ?: run {
                binding.settingsAdvancedCustomSensitivityEdit.setText(it)
            }
        }
        binding.settingsAdvancedCustomSensitivityEdit.onChanged().collect {
            viewModel.setValue(it?.toString() ?: return@collect)
        }
    }

    private fun setupError() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.isSensitivityValid.collect {
            binding.settingsAdvancedCustomSensitivityInput.error = if(it) null else {
                getString(R.string.bs_advanced_custom_sensitivity_invalid)
            }
            binding.settingsAdvancedCustomSensitivityInput.isErrorEnabled = !it
            binding.settingsAdvancedCustomSensitivityPositive.alpha = if(it) 1f else 0.5f
        }
    }

    private fun setupPositive() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsAdvancedCustomSensitivityPositive.setTextColor(accent)
        binding.settingsAdvancedCustomSensitivityPositive.onClicked().collect {
            viewModel.onPositiveClicked()
        }
    }

    private fun setupNegative() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsAdvancedCustomSensitivityNegative.setTextColor(accent)
        binding.settingsAdvancedCustomSensitivityNegative.onClicked().collect {
            viewModel.onNegativeClicked()
        }
    }

    private fun setupNeutral() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsAdvancedCustomSensitivityNeutral.setTextColor(accent)
        binding.settingsAdvancedCustomSensitivityNeutral.onClicked().collect {
            viewModel.onNeutralClicked()
        }
    }

    private fun setupRestart() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.restartService?.collect {
            sharedViewModel.restartService(requireContext())
        }
    }

}