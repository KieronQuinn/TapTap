package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.permissionprompt

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsRestoreNotificationPermissionBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val permissionGrant = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    private val arguments by navArgs<SettingsRestoreNotificationPermissionBottomSheetFragmentArgs>()
    private val viewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()

    private val isNotificationAccessGranted: Boolean
        get(){
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationPolicyAccessGranted
        }

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.restore_prompt_permission_title)
        message(text = getMessage())
        noAutoDismiss()
        positiveButton(R.string.restore_prompt_grant){
            permissionGrant.launch(getNotificationIntent())
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

    private fun checkPermission(){
        if(isNotificationAccessGranted){
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
                getString(R.string.restore_prompt_notification_permission_content_action, getString(actionGate.action.action.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> {
                getString(R.string.restore_prompt_notification_permission_content_gate, getString(actionGate.gate.gate.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> {
                getString(R.string.restore_prompt_notification_permission_content_when_gate, getString(actionGate.parentAction.action.nameRes), getString(actionGate.gate.gate.nameRes))
            }
        }
    }

    private fun getNotificationIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    private fun close(){
        findNavController().navigateUp()
    }

}