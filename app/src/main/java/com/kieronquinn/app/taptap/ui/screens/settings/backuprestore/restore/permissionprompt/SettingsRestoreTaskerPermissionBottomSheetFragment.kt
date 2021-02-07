package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.permissionprompt

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import kotlinx.coroutines.launch
import net.dinglisch.android.tasker.TaskerIntent
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsRestoreTaskerPermissionBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val permissionGrant = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    private val arguments by navArgs<SettingsRestoreTaskerPermissionBottomSheetFragmentArgs>()
    private val viewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.restore_prompt_permission_title)
        message(text = getMessage())
        noAutoDismiss()
        positiveButton(R.string.restore_prompt_grant){
            permissionGrant.launch(getTaskerIntent())
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

    private fun getTaskerIntent(): Intent {
        return Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //This seems to set the current tab. Misc is the 4th tab.
            putExtra("tno", 3)
        }
    }

    private fun checkPermission(){
        if(TaskerIntent.testStatus(context) == TaskerIntent.Status.OK){
            viewModel.viewModelScope.launch {
                viewModel.iterateAndMoveOn()
            }
            close()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun getMessage(): String {
        return when(val actionGate = arguments.actionGate){
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Action -> {
                getString(R.string.restore_prompt_tasker_content_action, getString(actionGate.action.action.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> {
                getString(R.string.restore_prompt_tasker_content_gate, getString(actionGate.gate.gate.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> {
                getString(R.string.restore_prompt_tasker_content_when_gate, getString(actionGate.parentAction.action.nameRes), getString(actionGate.gate.gate.nameRes))
            }
        }
    }
    
    private fun close(){
        findNavController().navigateUp()
    }

}