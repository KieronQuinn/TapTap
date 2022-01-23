package com.kieronquinn.app.taptap.ui.screens.disablecolumbus

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentDisableColumbusBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class DisableColumbusFragment: BoundFragment<FragmentDisableColumbusBinding>(FragmentDisableColumbusBinding::inflate) {

    private val viewModel by viewModel<DisableColumbusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupOpenSettings()
        setupPhoenix()
    }

    private fun setupMonet() {
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
        binding.disableColumbusOpenSettings.setTextColor(monet.getAccentColor(requireContext()))
        val fallbackBackground =
            if (requireContext().isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        binding.disableColumbusCard.setCardBackgroundColor(monet.getBackgroundColorSecondary(requireContext()) ?: fallbackBackground)
    }

    private fun setupOpenSettings() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.disableColumbusOpenSettings.onClicked().collect {
            viewModel.onOpenSettingsClicked()
        }
    }

    private fun setupPhoenix() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.phoenixBus.collect {
            viewModel.phoenix()
        }
    }

}