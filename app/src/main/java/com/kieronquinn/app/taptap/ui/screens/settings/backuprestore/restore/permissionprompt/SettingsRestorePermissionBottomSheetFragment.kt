package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.permissionprompt

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.toCamelCase
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsRestorePermissionBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val permissionGrant = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ results ->
        if(results.all { it.value }){
            viewModel.viewModelScope.launch {
                viewModel.iterateAndMoveOn()
            }
            close()
        }
    }

    private val arguments by navArgs<SettingsRestorePermissionBottomSheetFragmentArgs>()
    private val viewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()

    private val permissionLabel by lazy {
        arguments.requirement.permission.run {
            joinToString { it.substring(lastIndexOf(".") + 1, it.length).toCamelCase() }
        }
    }

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.restore_prompt_permission_title)
        message(text = getMessage())
        noAutoDismiss()
        positiveButton(R.string.restore_prompt_grant){
            permissionGrant.launch(arguments.requirement.permission.toTypedArray())
        }
        negativeButton(R.string.restore_prompt_skip){
            viewModel.viewModelScope.launch {
                viewModel.skipAndMoveOn(SettingsBackupRestoreRestoreViewModel.SkippedReason.USER_SKIPPED)
            }
            close()
        }
        neutralButton(R.string.restore_prompt_cancel){
            close()
            viewModel.cancel()
        }
    }

    private fun getMessage(): String {
        return when(val actionGate = arguments.actionGate){
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Action -> {
                getString(R.string.restore_prompt_permission_content_action, getString(actionGate.action.action.nameRes), permissionLabel)
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> {
                getString(R.string.restore_prompt_permission_content_gate, getString(actionGate.gate.gate.nameRes), permissionLabel)
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> {
                getString(R.string.restore_prompt_permission_content_when_gate, getString(actionGate.parentAction.action.nameRes), getString(actionGate.gate.gate.nameRes), permissionLabel)
            }
        }
    }
    
    private fun close(){
        findNavController().navigateUp()
    }

}