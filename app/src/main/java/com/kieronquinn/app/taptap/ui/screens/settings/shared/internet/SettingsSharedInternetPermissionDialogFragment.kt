package com.kieronquinn.app.taptap.ui.screens.settings.shared.internet

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentDialogInternetPermissionBinding
import com.kieronquinn.app.taptap.ui.base.BaseDialogFragment
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedInternetPermissionDialogFragment: BaseDialogFragment<FragmentDialogInternetPermissionBinding>(FragmentDialogInternetPermissionBinding::inflate) {

    private val viewModel by viewModel<SettingsSharedInternetPermissionDialogViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        setupMonet()
        setupButtons()
        setupDismiss()
    }

    private fun setupMonet(){
        val background = monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext())
        dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog_internet_permission)?.apply {
            setTint(background)
        })
        val accent = monet.getAccentColor(requireContext())
        binding.dialogInternetPermissionIcon.imageTintList = ColorStateList.valueOf(accent)
        binding.dialogInternetPermissionAlways.run {
            strokeColor = ColorStateList.valueOf(accent)
            overrideRippleColor(accent)
        }
        binding.dialogInternetPermissionRunning.run {
            strokeColor = ColorStateList.valueOf(accent)
            overrideRippleColor(accent)
        }
    }

    private fun setupButtons(){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.dialogInternetPermissionAlways.onClicked().collect {
                viewModel.onAllowAlwaysClicked()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.dialogInternetPermissionRunning.onClicked().collect {
                viewModel.onAllowRunningClicked()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.dialogInternetPermissionDeny.onClicked().collect {
                viewModel.onDenyClicked()
            }
        }
    }

    private fun setupDismiss() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.dismissBus.collect {
            dismissWithAnimation()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.attributes.windowAnimations = R.style.DialogAnimation
        }
        return dialog
    }

}