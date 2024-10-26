package com.kieronquinn.app.taptap.ui.screens.setup.info

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSetupInfoBinding
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
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
        binding.setupInfoNextContainer.backgroundTintList =
            ColorStateList.valueOf(monet.getPrimaryColor(requireContext(), !requireContext().isDarkMode))
        val accentColor = monet.getAccentColor(requireContext())
        binding.setupInfoNext.run {
            iconTint = ColorStateList.valueOf(accentColor)
            setTextColor(accentColor)
        }
        binding.setupInfoCardDonate.applyBackgroundTint(monet)
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

    private fun setupNext() = whenResumed {
        binding.setupInfoNext.onClicked().collect {
            viewModel.onNextClicked(requireContext())
        }
    }

    private fun setupSource() = whenResumed {
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