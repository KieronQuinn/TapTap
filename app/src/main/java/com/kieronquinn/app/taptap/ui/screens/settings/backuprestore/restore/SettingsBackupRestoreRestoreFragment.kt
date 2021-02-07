package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieDrawable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentBackupRestoreRestoreBinding
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.observe
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsBackupRestoreRestoreFragment: BoundFragment<FragmentBackupRestoreRestoreBinding>(FragmentBackupRestoreRestoreBinding::class.java) {

    private val viewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()
    private val arguments by navArgs<SettingsBackupRestoreRestoreFragmentArgs>()
    override val disableToolbarBackground: Boolean = true

    private val avdDownToTick by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_down_to_tick) as AnimatedVectorDrawable
    }

    private val avdDownToError by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_down_to_error) as AnimatedVectorDrawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            root.applySystemWindowInsetsToPadding(top = true, bottom = true)
            fragmentBackupRestoreRestoreLottie.run {
                clipToOutline = true
                repeatCount = LottieDrawable.INFINITE
                speed = 0.5f
                reverseAnimationSpeed()
                playAnimation()
            }
            fragmentBackupRestoreRestoreButton.setOnClickListener {
                viewModel.onCloseClicked(this@SettingsBackupRestoreRestoreFragment)
            }
        }
        with(viewModel){
            state.asLiveData().observe(viewLifecycleOwner){
                when(it){
                    is SettingsBackupRestoreRestoreViewModel.State.Running -> {
                        avdDownToTick.reset()
                        binding.fragmentBackupRestoreRestoreAvd.setImageDrawable(avdDownToTick)
                        binding.fragmentBackupRestoreRestoreLottie.playAnimation()
                        binding.fragmentBackupRestoreRestoreProgress.isVisible = true
                        binding.fragmentBackupRestoreRestoreButton.isVisible = false
                        binding.fragmentBackupRestoreRestoreSkipped.isVisible = false
                    }
                    is SettingsBackupRestoreRestoreViewModel.State.Done -> {
                        avdDownToTick.reset()
                        binding.fragmentBackupRestoreRestoreAvd.setImageDrawable(avdDownToTick)
                        binding.fragmentBackupRestoreRestoreLottie.pauseAnimation()
                        avdDownToTick.start()
                        binding.fragmentBackupRestoreRestoreProgress.isVisible = false
                        binding.fragmentBackupRestoreRestoreButton.isVisible = true
                        binding.fragmentBackupRestoreRestoreSkipped.isVisible = it.skipped.isNotEmpty()
                        binding.fragmentBackupRestoreRestoreSkipped.text = resources.getQuantityString(R.plurals.settings_backuprestore_restore_skipped, it.skipped.size, it.skipped.size)
                        binding.fragmentBackupRestoreRestoreSkipped.setOnClickListener {  _ ->
                            if(it.skipped.isNotEmpty()){
                                viewModel.showSkippedDialog(this@SettingsBackupRestoreRestoreFragment, it.skipped)
                            }
                        }
                    }
                    is SettingsBackupRestoreRestoreViewModel.State.Error -> {
                        avdDownToError.reset()
                        binding.fragmentBackupRestoreRestoreAvd.setImageDrawable(avdDownToError)
                        binding.fragmentBackupRestoreRestoreLottie.pauseAnimation()
                        avdDownToError.start()
                        binding.fragmentBackupRestoreRestoreProgress.isVisible = false
                        binding.fragmentBackupRestoreRestoreButton.isVisible = true
                        binding.fragmentBackupRestoreRestoreSkipped.isVisible = false
                    }
                    is SettingsBackupRestoreRestoreViewModel.State.PermissionCheck -> {
                        handlePermissionCheck(this@SettingsBackupRestoreRestoreFragment, it.backupJson, it.toCheck, it.skipped)
                    }
                    is SettingsBackupRestoreRestoreViewModel.State.Write -> {
                        handleWrite(it.backupJson, it.skipped)
                    }
                    is SettingsBackupRestoreRestoreViewModel.State.Cancelled -> {
                        viewModel.onCloseClicked(this@SettingsBackupRestoreRestoreFragment)
                    }
                }
            }
            getTitle(requireContext()).asLiveData().observe(viewLifecycleOwner){
                binding.fragmentBackupRestoreRestoreTitle.text = it
            }
            getContent(requireContext()).asLiveData().observe(viewLifecycleOwner){
                binding.fragmentBackupRestoreRestoreContent.text = it
            }
            startRestore(requireContext(), arguments.restoreUri)
        }
    }

    override fun onBackPressed(): Boolean {
        viewModel.cancel()
        return true
    }

}