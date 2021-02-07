package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieDrawable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentBackupRestoreBackupBinding
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.observe
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsBackupRestoreBackupFragment: BoundFragment<FragmentBackupRestoreBackupBinding>(FragmentBackupRestoreBackupBinding::class.java) {

    private val viewModel by viewModel<SettingsBackupRestoreBackupViewModel>()
    private val arguments by navArgs<SettingsBackupRestoreBackupFragmentArgs>()

    override val disableToolbarBackground: Boolean = true

    private val avdUpToSuccess by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_up_to_cloud) as AnimatedVectorDrawable
    }

    private val avdUpToError by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_up_to_error) as AnimatedVectorDrawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            root.applySystemWindowInsetsToPadding(top = true, bottom = true)
            fragmentBackupRestoreBackupLottie.run {
                clipToOutline = true
                repeatCount = LottieDrawable.INFINITE
                speed = 0.5f
                playAnimation()
            }
            fragmentBackupRestoreBackupButton.setOnClickListener {
                viewModel.onCloseClicked(this@SettingsBackupRestoreBackupFragment)
            }
        }
        with(viewModel){
            state.asLiveData().observe(viewLifecycleOwner){
                when(it){
                    is SettingsBackupRestoreBackupViewModel.State.Running -> {
                        avdUpToSuccess.reset()
                        binding.fragmentBackupRestoreBackupAvd.setImageDrawable(avdUpToSuccess)
                        binding.fragmentBackupRestoreBackupLottie.playAnimation()
                        binding.fragmentBackupRestoreBackupProgress.isVisible = true
                        binding.fragmentBackupRestoreBackupButton.isVisible = false
                    }
                    is SettingsBackupRestoreBackupViewModel.State.Done -> {
                        avdUpToSuccess.reset()
                        binding.fragmentBackupRestoreBackupAvd.setImageDrawable(avdUpToSuccess)
                        binding.fragmentBackupRestoreBackupLottie.pauseAnimation()
                        avdUpToSuccess.start()
                        binding.fragmentBackupRestoreBackupProgress.isVisible = false
                        binding.fragmentBackupRestoreBackupButton.isVisible = true
                    }
                    is SettingsBackupRestoreBackupViewModel.State.Error -> {
                        avdUpToError.reset()
                        binding.fragmentBackupRestoreBackupAvd.setImageDrawable(avdUpToError)
                        binding.fragmentBackupRestoreBackupLottie.pauseAnimation()
                        avdUpToError.start()
                        binding.fragmentBackupRestoreBackupProgress.isVisible = false
                        binding.fragmentBackupRestoreBackupButton.isVisible = true
                    }
                    is SettingsBackupRestoreBackupViewModel.State.Cancelled -> {
                        viewModel.onCloseClicked(this@SettingsBackupRestoreBackupFragment)
                    }
                }
            }
            getTitle(requireContext()).asLiveData().observe(viewLifecycleOwner){
                binding.fragmentBackupRestoreBackupTitle.text = it
            }
            getContent(requireContext()).asLiveData().observe(viewLifecycleOwner){
                binding.fragmentBackupRestoreBackupContent.text = it
            }
            createBackup(requireContext(), arguments.backupUri)
        }
    }

    override fun onBackPressed(): Boolean {
        viewModel.cancel()
        return false
    }

}