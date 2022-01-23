package com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedSnapchatBinding
import com.kieronquinn.app.taptap.models.shared.ARG_NAME_SHARED_ARGUMENT
import com.kieronquinn.app.taptap.repositories.snapchat.SnapchatRepository
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat.SettingsSharedSnapchatViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedSnapchatFragment :
    BoundFragment<FragmentSettingsSharedSnapchatBinding>(FragmentSettingsSharedSnapchatBinding::inflate),
    BackAvailable {

    companion object {
        const val FRAGMENT_RESULT_KEY_SNAPCHAT = "fragment_result_snapchat"
    }

    private val args by navArgs<SettingsSharedSnapchatFragmentArgs>()
    private val viewModel by viewModel<SettingsSharedSnapchatViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupState()
        setupRoot()
        setupNonRoot()
        setupIncompatible()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun setupMonet() {
        binding.settingsSharedSnapchatLoadingProgress.applyMonet()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when (state) {
            is State.Loading -> {
                binding.settingsSharedSnapchatSetupRoot.isVisible = false
                binding.settingsSharedSnapchatSetupNoRoot.isVisible = false
                binding.settingsSharedSnapchatSetupIncompatible.isVisible = false
                binding.settingsSharedSnapchatLoading.isVisible = true
            }
            is State.Loaded -> {
                handleLoaded(state.state)
            }
        }
    }

    private fun handleLoaded(state: SnapchatRepository.QuickTapToSnapState) {
        setFragmentResult(
            FRAGMENT_RESULT_KEY_SNAPCHAT,
            formResultBundle(state == SnapchatRepository.QuickTapToSnapState.AVAILABLE)
        )
        when (state) {
            SnapchatRepository.QuickTapToSnapState.AVAILABLE -> {
                binding.settingsSharedSnapchatSetupRoot.isVisible = false
                binding.settingsSharedSnapchatSetupNoRoot.isVisible = false
                binding.settingsSharedSnapchatSetupIncompatible.isVisible = false
                binding.settingsSharedSnapchatLoading.isVisible = true
                viewModel.popBackstack(true)
            }
            SnapchatRepository.QuickTapToSnapState.NEEDS_SETUP_ROOT -> {
                binding.settingsSharedSnapchatSetupRoot.isVisible = true
                binding.settingsSharedSnapchatSetupNoRoot.isVisible = false
                binding.settingsSharedSnapchatSetupIncompatible.isVisible = false
                binding.settingsSharedSnapchatLoading.isVisible = false
            }
            SnapchatRepository.QuickTapToSnapState.NEEDS_SETUP -> {
                binding.settingsSharedSnapchatSetupRoot.isVisible = false
                binding.settingsSharedSnapchatSetupNoRoot.isVisible = true
                binding.settingsSharedSnapchatSetupIncompatible.isVisible = false
                binding.settingsSharedSnapchatLoading.isVisible = false
            }
            SnapchatRepository.QuickTapToSnapState.UNAVAILABLE -> {
                binding.settingsSharedSnapchatSetupRoot.isVisible = false
                binding.settingsSharedSnapchatSetupNoRoot.isVisible = false
                binding.settingsSharedSnapchatSetupIncompatible.isVisible = true
                binding.settingsSharedSnapchatLoading.isVisible = false
            }
            SnapchatRepository.QuickTapToSnapState.ERROR -> {
                Toast.makeText(requireContext(), R.string.settings_shared_snapchat_setup_error, Toast.LENGTH_LONG).show()
                viewModel.popBackstack(true)
            }
        }
    }

    private fun setupNonRoot() {
        binding.settingsSharedSnapchatSetupNoRootCard.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(requireContext()))
        binding.settingsSharedSnapchatSetupNoRootButton.setTextColor(monet.getAccentColor(requireContext()))
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.settingsSharedSnapchatSetupNoRootButton.onClicked().collect {
                viewModel.onInstructionsClicked()
            }
        }
    }


    private fun setupRoot() {
        binding.settingsSharedSnapchatSetupRootCard.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(requireContext()))
        binding.settingsSharedSnapchatSetupRootButton.setTextColor(monet.getAccentColor(requireContext()))
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.settingsSharedSnapchatSetupRootButton.onClicked().collect {
                viewModel.onSnapchatClicked(requireContext())
            }
        }
    }

    private fun setupIncompatible() {
        binding.settingsSharedSnapchatSetupIncompatibleCard.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(requireContext()))
        binding.settingsSharedSnapchatSetupIncompatibleButton.setTextColor(monet.getAccentColor(requireContext()))
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.settingsSharedSnapchatSetupIncompatibleButton.onClicked().collect {
                viewModel.popBackstack(false)
            }
        }
    }

    private fun formResultBundle(available: Boolean): Bundle {
        return bundleOf(
            FRAGMENT_RESULT_KEY_SNAPCHAT to available,
            ARG_NAME_SHARED_ARGUMENT to args.argument
        )
    }

}