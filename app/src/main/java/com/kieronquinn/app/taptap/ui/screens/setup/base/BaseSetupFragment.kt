package com.kieronquinn.app.taptap.ui.screens.setup.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.utils.extensions.onNavigationIconClicked
import kotlinx.coroutines.flow.collect

abstract class BaseSetupFragment<T: ViewBinding>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): BoundFragment<T>(inflate) {

    abstract val viewModel: BaseSetupViewModel
    open val toolbar: Toolbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
    }

    override fun onResume() {
        super.onResume()
        setupBack()
    }

    private fun setupMonet() {
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
    }

    private fun setupBack() {
        if(this is ProvidesBack) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                onBackPressed()
            }
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                toolbar?.onNavigationIconClicked()?.collect {
                    onBackPressed()
                }
            }
        }
    }

}