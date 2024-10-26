package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsBackupRestoreBackupBinding
import com.kieronquinn.app.taptap.repositories.backuprestore.BackupRepository
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup.SettingsBackupRestoreBackupViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsBackupRestoreBackupFragment :
    BoundFragment<FragmentSettingsBackupRestoreBackupBinding>(FragmentSettingsBackupRestoreBackupBinding::inflate), BackAvailable {

    private val args by navArgs<SettingsBackupRestoreBackupFragmentArgs>()
    private val viewModel by viewModel<SettingsBackupRestoreBackupViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupClose()
        setupState()
        viewModel.setBackupUri(args.backupUri)
    }

    private fun setupMonet() {
        binding.settingsBackupRestoreBackupProgress.applyMonet()
        binding.settingsBackupRestoreBackupIcon.imageTintList = ColorStateList.valueOf(monet.getAccentColor(requireContext()))
        binding.settingsBackupRestoreBackupClose.setTextColor(monet.getAccentColor(requireContext()))
    }

    private fun setupClose() = whenResumed {
        binding.settingsBackupRestoreBackupClose.onClicked().collect {
            viewModel.onCloseClicked()
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

    private fun handleState(state: State) {
        when(state){
            is State.BackingUp -> {
                binding.settingsBackupRestoreBackupTitle.text = getString(R.string.settings_backuprestore_backup_title)
                binding.settingsBackupRestoreBackupDesc.text = getString(R.string.settings_backuprestore_backup_desc)
                binding.settingsBackupRestoreBackupProgress.isVisible = true
                binding.settingsBackupRestoreBackupIcon.isVisible = false
                binding.settingsBackupRestoreBackupClose.isVisible = false
            }
            is State.Finished -> {
                if(state.result is BackupRepository.BackupResult.Success){
                    binding.settingsBackupRestoreBackupTitle.text = getString(R.string.settings_backuprestore_backup_done_title)
                    binding.settingsBackupRestoreBackupDesc.text = getString(R.string.settings_backuprestore_backup_done_desc, state.result.file.name)
                    binding.settingsBackupRestoreBackupIcon.setImageResource(R.drawable.ic_check_circle)
                }else if(state.result is BackupRepository.BackupResult.Error){
                    binding.settingsBackupRestoreBackupTitle.text = getString(R.string.settings_backuprestore_backup_error_title)
                    binding.settingsBackupRestoreBackupDesc.text = getString(R.string.settings_backuprestore_backup_error_desc, getString(state.result.errorType.errorRes))
                    binding.settingsBackupRestoreBackupIcon.setImageResource(R.drawable.ic_error_circle)
                }
                binding.settingsBackupRestoreBackupProgress.isVisible = false
                binding.settingsBackupRestoreBackupIcon.isVisible = true
                binding.settingsBackupRestoreBackupClose.isVisible = true
            }
        }
    }

}