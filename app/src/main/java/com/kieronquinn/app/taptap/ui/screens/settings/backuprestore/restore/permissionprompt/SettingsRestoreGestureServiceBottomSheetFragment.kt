package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.permissionprompt

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.EXTRA_FRAGMENT_ARG_KEY
import com.kieronquinn.app.taptap.utils.extensions.EXTRA_SHOW_FRAGMENT_ARGUMENTS
import com.kieronquinn.app.taptap.utils.extensions.doesContainComponentName
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.main.SettingsMainViewModel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsRestoreGestureServiceBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val serviceEnable = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    private val arguments by navArgs<SettingsRestoreGestureServiceBottomSheetFragmentArgs>()
    private val viewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.restore_prompt_gesture_title)
        message(text = getMessage())
        noAutoDismiss()
        positiveButton(R.string.restore_prompt_grant){
            serviceEnable.launch(getAccessibilityIntent())
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

    private fun checkService(){
        if(Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.doesContainComponentName(SettingsMainViewModel.GESTURE_ACCESSIBILITY_SERVICE_COMPONENT) == true){
            viewModel.viewModelScope.launch {
                viewModel.iterateAndMoveOn()
            }
            close()
        }
    }

    override fun onResume() {
        super.onResume()
        checkService()
    }

    private fun getMessage(): String {
        return when(val actionGate = arguments.actionGate){
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Action -> {
                getString(R.string.restore_prompt_gesture_service_content_action, getString(actionGate.action.action.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> {
                getString(R.string.restore_prompt_gesture_service_content_gate, getString(actionGate.gate.gate.nameRes))
            }
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> {
                getString(R.string.restore_prompt_gesture_service_content_when_gate, getString(actionGate.parentAction.action.nameRes), getString(actionGate.gate.gate.nameRes))
            }
        }
    }

    private fun getAccessibilityIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapGestureAccessibilityService::class.java.name).flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
        }
    }
    
    private fun close(){
        findNavController().navigateUp()
    }

}