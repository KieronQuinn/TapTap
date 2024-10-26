package com.kieronquinn.app.taptap.ui.screens.setup.landing

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentSetupLandingBinding
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupLandingFragment: BaseSetupFragment<FragmentSetupLandingBinding>(FragmentSetupLandingBinding::inflate) {

    override val viewModel by viewModel<SetupLandingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupSkip()
        setupGetStarted()
    }

    private fun setupMonet() {
        val accent = monet.getAccentColor(requireContext())
        binding.setupLandingGetStarted.setTextColor(accent)
        binding.setupLandingSkipSetup.setTextColor(accent)
    }

    private fun setupSkip() = whenResumed {
        binding.setupLandingSkipSetup.onClicked().collect {
            viewModel.onSkipClicked()
        }
    }

    private fun setupGetStarted() = whenResumed {
        binding.setupLandingGetStarted.onClicked().collect {
            viewModel.onStartClicked()
        }
    }

}