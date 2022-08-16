package com.kieronquinn.app.taptap.ui.screens.setup.notifications

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.databinding.FragmentSetupNotificationsBinding
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.replaceColour
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupNotificationsFragment: BaseSetupFragment<FragmentSetupNotificationsBinding>(FragmentSetupNotificationsBinding::inflate), ProvidesBack {

    override val toolbar by lazy {
        binding.toolbar
    }

    override val viewModel by viewModel<SetupNotificationsViewModel>()

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onPermissionResult(requireContext(), it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupToolbar()
        setupMonet()
        setupGrant()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermission(requireContext())
    }

    private fun setupMonet() {
        val accentColor = monet.getAccentColor(requireContext())
        binding.setupNotificationsRequest.run {
            iconTint = ColorStateList.valueOf(accentColor)
            setTextColor(accentColor)
        }
        binding.setupNotificationsLottie.run {
            replaceColour("*", "**", replaceWith = accentColor)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setupWithScrollableView(binding.setupNotificationsScrollable)
    }

    private fun setupGrant() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.setupNotificationsRequest.onClicked().collect {
            viewModel.onGrantClicked(notificationPermissionRequest)
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

}