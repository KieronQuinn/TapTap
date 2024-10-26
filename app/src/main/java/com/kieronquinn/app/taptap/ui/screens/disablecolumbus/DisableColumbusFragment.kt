package com.kieronquinn.app.taptap.ui.screens.disablecolumbus

import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.view.View
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentDisableColumbusBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.androidx.viewmodel.ext.android.viewModel

class DisableColumbusFragment: BoundFragment<FragmentDisableColumbusBinding>(FragmentDisableColumbusBinding::inflate) {

    private val viewModel by viewModel<DisableColumbusViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupOpenSettings()
        setupPhoenix()
        setupInfo()
    }

    private fun setupMonet() {
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
        binding.disableColumbusOpenSettings.setTextColor(monet.getAccentColor(requireContext()))
        binding.disableColumbusCard.applyBackgroundTint(monet)
    }

    private fun setupOpenSettings() = whenResumed {
        binding.disableColumbusOpenSettings.onClicked().collect {
            viewModel.onOpenSettingsClicked()
        }
    }

    private fun setupPhoenix() = whenResumed {
        viewModel.phoenixBus.collect {
            viewModel.phoenix()
        }
    }

    private fun setupInfo() = with(binding.disableColumbusContent) {
        text = Html.fromHtml(
            getString(R.string.modal_disable_columbus_content),
            Html.FROM_HTML_MODE_LEGACY
        )
        setLinkTextColor(monet.getAccentColor(context))
        Linkify.addLinks(this, Linkify.ALL)
        movementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener { _, url ->
            viewModel.onLinkClicked(url)
            true
        }
    }

}