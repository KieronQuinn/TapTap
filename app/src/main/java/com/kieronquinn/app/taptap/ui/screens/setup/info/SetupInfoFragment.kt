package com.kieronquinn.app.taptap.ui.screens.setup.info

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSetupInfoBinding
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupInfoFragment: BaseSetupFragment<FragmentSetupInfoBinding>(FragmentSetupInfoBinding::inflate), ProvidesBack {

    override val viewModel by viewModel<SetupInfoViewModel>()

    override val toolbar by lazy {
        binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupToolbar()
        setupContent()
        setupNext()
        setupSource()
        setupInsets()
    }

    private fun setupMonet() {
        val fallbackBackground =
            if (requireContext().isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        val secondary = monet.getBackgroundColorSecondary(requireContext())
            ?: ContextCompat.getColor(requireContext(), fallbackBackground)
        binding.setupInfoNextContainer.setBackgroundColor(secondary)
        val accentColor = monet.getAccentColor(requireContext())
        binding.setupInfoNext.run {
            iconTint = ColorStateList.valueOf(accentColor)
            setTextColor(accentColor)
        }
        binding.setupInfoCardSource.setCardBackgroundColor(monet.getPrimaryColor(requireContext()))
        binding.setupInfoCardDonate.setCardBackgroundColor(secondary)
    }

    private fun setupContent() = with(binding.setupFossInfoContent) {
        text = Html.fromHtml(
            getString(R.string.setup_foss_info_content),
            Html.FROM_HTML_MODE_LEGACY
        )
        setLinkTextColor(monet.getAccentColor(context))
        Linkify.addLinks(this, Linkify.ALL)
        movementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener { _, url ->
            viewModel.onLinkClicked(url)
            true
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setupWithScrollableView(binding.setupInfoScrollable)
    }

    private fun setupNext() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.setupInfoNext.onClicked().collect {
            viewModel.onNextClicked(requireContext())
        }
    }

    private fun setupSource() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.setupInfoCardSource.onClicked().collect {
            viewModel.onSourceClicked()
        }
    }

    private fun setupInsets() {
        binding.toolbar.onApplyInsets { view, insets ->
            view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
        }
        binding.setupInfoNextContainer.onApplyInsets { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

}