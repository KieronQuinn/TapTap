package com.kieronquinn.app.taptap.ui.screens.settings.update

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsUpdateBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel.FabState
import com.kieronquinn.app.taptap.ui.screens.settings.update.SettingsUpdateViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor
import io.noties.markwon.Markwon
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SettingsUpdateFragment: BoundFragment<FragmentSettingsUpdateBinding>(FragmentSettingsUpdateBinding::inflate), BackAvailable {

    private val viewModel by viewModel<SettingsUpdateViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val args by navArgs<SettingsUpdateFragmentArgs>()
    private val markwon by inject<Markwon>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupState()
        setupStartInstall()
        setupGitHubButton()
        setupFabState()
        setupFabClick()
        viewModel.setupWithRelease(args.release)
    }

    private fun setupMonet() {
        val accent = monet.getAccentColor(requireContext())
        val primary = monet.getPrimaryColor(requireContext())
        binding.settingsUpdateCard.applyBackgroundTint(monet)
        binding.settingsUpdateStartInstall.setTextColor(accent)
        binding.settingsUpdateStartInstall.overrideRippleColor(accent)
        binding.settingsUpdateProgress.applyMonet()
        binding.settingsUpdateProgressIndeterminate.applyMonet()
        binding.settingsUpdateIcon.imageTintList = ColorStateList.valueOf(accent)
        binding.settingsUpdateDownloadBrowser.setTextColor(accent)
    }

    private fun setupStartInstall() = whenResumed {
        binding.settingsUpdateStartInstall.onClicked().collect {
            viewModel.startInstall()
        }
    }

    private fun setupGitHubButton() = whenResumed {
        binding.settingsUpdateDownloadBrowser.onClicked().collect {
            viewModel.onDownloadBrowserClicked(args.release.gitHubUrl)
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State){
        when(state){
            is State.Loading -> setupWithLoading()
            is State.Info -> setupWithInfo(state)
            is State.StartDownload -> setupWithStartDownload()
            is State.Downloading -> setupWithDownloading(state)
            is State.Done, is State.StartInstall -> setupWithDone()
            is State.Failed -> setupWithFailed()
        }
    }

    private fun setupWithLoading() {
        binding.settingsUpdateInfo.isVisible = false
        binding.settingsUpdateProgress.isVisible = false
        binding.settingsUpdateProgressIndeterminate.isVisible = true
        binding.settingsUpdateTitle.isVisible = true
        binding.settingsUpdateIcon.isVisible = false
        binding.settingsUpdateStartInstall.isVisible = false
        binding.settingsUpdateTitle.setText(R.string.settings_update_loading)
    }

    private fun setupWithInfo(info: State.Info){
        val release = info.release
        binding.settingsUpdateInfo.isVisible = true
        binding.settingsUpdateProgress.isVisible = false
        binding.settingsUpdateProgressIndeterminate.isVisible = false
        binding.settingsUpdateTitle.isVisible = false
        binding.settingsUpdateIcon.isVisible = false
        binding.settingsUpdateStartInstall.isVisible = false
        binding.settingsUpdateHeading.text = getString(R.string.settings_update_heading, release.versionName)
        binding.settingsUpdateSubheading.text = getString(R.string.settings_update_subheading, BuildConfig.VERSION_NAME)
        binding.settingsUpdateBody.text = markwon.toMarkdown(release.body)
        binding.settingsUpdateInfo.applyBottomInsets(binding.root, resources.getDimension(R.dimen.container_fab_margin).toInt())
        whenResumed {
            binding.settingsUpdateDownloadBrowser.onClicked().collect {
                viewModel.onDownloadBrowserClicked(release.gitHubUrl)
            }
        }
    }

    private fun setupWithStartDownload() {
        binding.settingsUpdateInfo.isVisible = false
        binding.settingsUpdateProgress.isVisible = false
        binding.settingsUpdateProgressIndeterminate.isVisible = true
        binding.settingsUpdateTitle.isVisible = true
        binding.settingsUpdateIcon.isVisible = false
        binding.settingsUpdateStartInstall.isVisible = false
        binding.settingsUpdateTitle.setText(R.string.update_downloader_downloading_title)
    }

    private fun setupWithDownloading(state: State.Downloading) {
        binding.settingsUpdateInfo.isVisible = false
        binding.settingsUpdateProgress.isVisible = true
        binding.settingsUpdateProgressIndeterminate.isVisible = false
        binding.settingsUpdateTitle.isVisible = true
        binding.settingsUpdateIcon.isVisible = false
        binding.settingsUpdateStartInstall.isVisible = false
        binding.settingsUpdateProgress.progress = (state.progress * 100).roundToInt()
        binding.settingsUpdateTitle.setText(R.string.update_downloader_downloading_title)
    }

    private fun setupWithDone() {
        binding.settingsUpdateInfo.isVisible = false
        binding.settingsUpdateProgress.isVisible = false
        binding.settingsUpdateProgressIndeterminate.isVisible = false
        binding.settingsUpdateTitle.isVisible = true
        binding.settingsUpdateIcon.isVisible = true
        binding.settingsUpdateStartInstall.isVisible = true
        binding.settingsUpdateTitle.setText(R.string.settings_update_done)
        binding.settingsUpdateIcon.setImageResource(R.drawable.ic_update_download_done)
    }

    private fun setupWithFailed() {
        binding.settingsUpdateInfo.isVisible = false
        binding.settingsUpdateProgress.isVisible = false
        binding.settingsUpdateProgressIndeterminate.isVisible = false
        binding.settingsUpdateTitle.isVisible = true
        binding.settingsUpdateIcon.isVisible = true
        binding.settingsUpdateStartInstall.isVisible = true
        binding.settingsUpdateTitle.setText(R.string.update_downloader_downloading_failed)
        binding.settingsUpdateIcon.setImageResource(R.drawable.ic_error_circle)
    }

    private fun setupFabState() {
        handleFabState(viewModel.showFab.value)
        whenResumed {
            viewModel.showFab.collect {
                handleFabState(it)
            }
        }
    }

    private fun handleFabState(showFab: Boolean){
        if(showFab){
            sharedViewModel.setFabState(FabState.Shown(FabState.FabAction.DOWNLOAD))
        }else{
            sharedViewModel.setFabState(FabState.Hidden)
        }
    }

    private fun setupFabClick() = whenResumed {
        sharedViewModel.fabClicked.collect {
            if(it != FabState.FabAction.DOWNLOAD) return@collect
            viewModel.startDownload()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
        sharedViewModel.setFabState(FabState.Hidden)
    }

}