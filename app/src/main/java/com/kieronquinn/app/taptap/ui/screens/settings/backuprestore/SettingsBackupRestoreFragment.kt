package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.databinding.FragmentSettingsBackupRestoreBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsBackupRestoreFragment: BoundFragment<FragmentSettingsBackupRestoreBinding>(FragmentSettingsBackupRestoreBinding::inflate), BackAvailable {

    private val viewModel by viewModel<SettingsBackupRestoreViewModel>()

    private val backupFilePickerLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        if(it == null) return@registerForActivityResult
        viewModel.onBackupFileClicked(it)
    }

    private val restoreFilePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        if(it == null) return@registerForActivityResult
        viewModel.onRestoreFileClicked(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackup()
        setupRestore()
        binding.root.applyBottomInsets(binding.root)
    }

    private fun setupBackup() {
        binding.settingsBackupRestoreBackup.applyBackgroundTint(monet)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.settingsBackupRestoreBackup.onClicked().collect {
                viewModel.onBackupClicked(backupFilePickerLauncher)
            }
        }
    }

    private fun setupRestore() {
        binding.settingsBackupRestoreRestore.applyBackgroundTint(monet)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.settingsBackupRestoreRestore.onClicked().collect {
                viewModel.onRestoreClicked(restoreFilePickerLauncher)
            }
        }
    }

}