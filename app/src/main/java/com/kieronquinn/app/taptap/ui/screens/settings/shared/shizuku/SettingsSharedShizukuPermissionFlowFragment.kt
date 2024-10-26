package com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedShizukuPermissionFlowBinding
import com.kieronquinn.app.taptap.models.shared.ARG_NAME_SHARED_ARGUMENT
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedShizukuPermissionFlowFragment :
    BoundFragment<FragmentSettingsSharedShizukuPermissionFlowBinding>(
        FragmentSettingsSharedShizukuPermissionFlowBinding::inflate
    ), BackAvailable {

    private val viewModel by viewModel<SettingsSharedShizukuPermissionFlowViewModel>()
    private val args by navArgs<SettingsSharedShizukuPermissionFlowFragmentArgs>()

    companion object {
        const val FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION = "fragment_result_shizuku_permission"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupShizukuButton()
        setupSuiButton()
        setupState()
        setupScrollView()
        setupContent()
    }

    private fun setupContent() {
        val argument = args.argument
        val contentFormatted = when {
            argument.action != null -> R.string.settings_shared_shizuku_permission_flow_error_content_action
            argument.gate != null -> R.string.settings_shared_shizuku_permission_flow_error_content_gate
            //Default to action
            else -> R.string.settings_shared_shizuku_permission_flow_error_content_action
        }
        binding.settingsSharedShizukuPermissionFlowErrorContent.text = getString(R.string.settings_shared_shizuku_permission_flow_error_content, getString(contentFormatted))
    }

    private fun setupMonet() {
        binding.settingsSharedShizukuPermissionFlowLoadingProgress.applyMonet()
        binding.settingsSharedShizukuPermissionFlowErrorCardSui.applyBackgroundTint(monet)
        binding.settingsSharedShizukuPermissionFlowErrorCardShizuku.applyBackgroundTint(monet)
    }

    private fun setupShizukuButton() = whenResumed {
        binding.settingsSharedShizukuPermissionFlowErrorCardShizukuButton.onClicked().collect {
            viewModel.onShizukuClicked(requireContext())
        }
    }

    private fun setupScrollView() = with(binding.settingsSharedShizukuPermissionFlowError) {
        applyBottomInsets(binding.root)
    }

    private fun setupSuiButton() = whenResumed {
        binding.settingsSharedShizukuPermissionFlowErrorCardSuiButton.onClicked().collect {
            viewModel.onSuiClicked(requireContext())
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when (state) {
            is State.Loading -> {
                binding.settingsSharedShizukuPermissionFlowLoading.isVisible = true
                binding.settingsSharedShizukuPermissionFlowError.isVisible = false
            }
            is State.PermissionGranted -> {
                binding.settingsSharedShizukuPermissionFlowLoading.isVisible = true
                binding.settingsSharedShizukuPermissionFlowError.isVisible = false
                setFragmentResult(FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION, formResultBundle(true))
                viewModel.popBackstack()
            }
            is State.NoShizuku -> {
                binding.settingsSharedShizukuPermissionFlowLoading.isVisible = false
                binding.settingsSharedShizukuPermissionFlowError.isVisible = true
                setFragmentResult(FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION, formResultBundle(false))
                //Don't pop back up as there's details
            }
        }
    }

    private fun formResultBundle(permissionGranted: Boolean): Bundle {
        return bundleOf(
            FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION to permissionGranted,
            ARG_NAME_SHARED_ARGUMENT to args.argument
        )
    }

}