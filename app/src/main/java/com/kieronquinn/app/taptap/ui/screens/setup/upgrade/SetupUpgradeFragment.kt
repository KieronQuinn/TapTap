package com.kieronquinn.app.taptap.ui.screens.setup.upgrade

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentSetupUpgradeBinding
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupUpgradeFragment: BaseSetupFragment<FragmentSetupUpgradeBinding>(FragmentSetupUpgradeBinding::inflate) {

    override val viewModel by viewModel<SetupUpgradeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        viewModel.startUpgrade(requireContext())
    }

    private fun setupMonet() {
        binding.setupUpgradeLoadingProgress.applyMonet()
    }

}