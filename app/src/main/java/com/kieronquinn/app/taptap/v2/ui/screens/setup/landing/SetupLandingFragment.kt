package com.kieronquinn.app.taptap.v2.ui.screens.setup.landing

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentSetupLandingBinding
import com.kieronquinn.app.taptap.v2.components.base.BoundFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SetupLandingFragment: BoundFragment<FragmentSetupLandingBinding>(FragmentSetupLandingBinding::class.java) {

    private val viewModel by viewModel<SetupLandingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            landingButtonGetStarted.setOnClickListener {
                viewModel.onGetStartedClicked(this@SetupLandingFragment)
            }
            landingButtonSkipSetup.setOnClickListener {
                viewModel.onSkipSetupClicked(this@SetupLandingFragment)
            }
        }
    }

}