package com.kieronquinn.app.taptap.ui.screens.setup.complete

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSetupCompleteBinding
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.replaceColour
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupCompleteFragment: BaseSetupFragment<FragmentSetupCompleteBinding>(FragmentSetupCompleteBinding::inflate), ProvidesBack {

    override val viewModel by viewModel<SetupCompleteViewModel>()

    override val toolbar by lazy {
        binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupMonet()
        setupLottie()
        setupClose()
        setupInsets()
    }

    private fun setupToolbar() {
        binding.toolbar.setupWithScrollableView(binding.setupCompleteScrollable)
    }

    private fun setupMonet() {
        val fallbackBackground =
            if (requireContext().isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        val secondary = monet.getBackgroundColorSecondary(requireContext())
            ?: ContextCompat.getColor(requireContext(), fallbackBackground)
        binding.setupCompleteCard.setCardBackgroundColor(secondary)
        val accent = monet.getAccentColor(requireContext())
        binding.setupCompleteClose.setTextColor(accent)
        binding.setupCompleteClose.overrideRippleColor(accent)
    }

    private fun setupLottie() = with(binding.setupCompleteLottie) {
        val accent = monet.getAccentColor(requireContext(), false)
        replaceColour("Background Circle (Blue)", "**", replaceWith = accent)
        replaceColour("Background(Blue)", "**", replaceWith = accent)
        playAnimation()
    }

    private fun setupClose() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.setupCompleteClose.onClicked().collect {
            viewModel.onCloseClicked()
        }
    }

    private fun setupInsets() {
        binding.toolbar.onApplyInsets { view, insets ->
            view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

}