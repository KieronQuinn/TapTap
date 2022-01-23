package com.kieronquinn.app.taptap.ui.screens.reachability

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentReachabilityBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.onLongClicked
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReachabilityFragment: BoundFragment<FragmentReachabilityBinding>(FragmentReachabilityBinding::inflate) {

    private val viewModel by viewModel<ReachabilityViewModel>()

    private val minButtonHeight by lazy {
        resources.getDimension(R.dimen.reachability_button_size) + resources.getDimension(R.dimen.margin_16) + resources.getDimension(R.dimen.margin_16)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val background = monet.getBackgroundColor(requireContext())
        view.setBackgroundColor(background)
        requireActivity().window.run {
            statusBarColor = background
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                navigationBarColor = background
            }
        }
        viewModel.sendStartAction()
        setupMonet()
        setupHandedness()
        setupNotificationsClick()
        setupQuickSettingsClick()
        setupNotificationsLongClick()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            if(!viewModel.getHasLeftHandedSet()){
                Toast.makeText(requireContext(), R.string.reachability_left_handed_info, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupMonet() {
        val accent = ColorStateList.valueOf(monet.getAccentColor(requireContext()))
        binding.reachabilityNotifications.iconTint = accent
        binding.reachabilityQuickSettings.iconTint = accent
    }

    private fun setupHandedness() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.isLeftHanded.collect {
            if(it) {
                binding.reachabilityContainer.gravity = Gravity.BOTTOM or Gravity.START
            }else{
                binding.reachabilityContainer.gravity = Gravity.BOTTOM or Gravity.END
            }
        }
    }

    private fun setupNotificationsClick() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.reachabilityNotifications.onClicked().collect {
            viewModel.onNotificationsClicked()
        }
    }

    private fun setupQuickSettingsClick() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.reachabilityQuickSettings.onClicked().collect {
            viewModel.onQuickSettingsClicked()
        }
    }

    private fun setupNotificationsLongClick() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.reachabilityNotifications.onLongClicked().collect {
            viewModel.onNotificationsLongClicked()
        }
    }

    fun onWindowAttributesChanged(height: Int) {
        lifecycleScope.launchWhenResumed {
            if(height < minButtonHeight){
                binding.reachabilityNotifications.visibility = View.GONE
                binding.reachabilityQuickSettings.visibility = View.GONE
            }else{
                binding.reachabilityNotifications.visibility = View.VISIBLE
                binding.reachabilityQuickSettings.visibility = View.VISIBLE
            }
        }
    }

    fun onExitMultiWindow(providedApp: String?) {
        lifecycleScope.launchWhenResumed {
            viewModel.getCurrentApp() ?: providedApp?.let {
                requireContext().packageManager.getLaunchIntentForPackage(it)?.run {
                    startActivity(this)
                }
            }
            requireActivity().finishAndRemoveTask()
        }
    }

}