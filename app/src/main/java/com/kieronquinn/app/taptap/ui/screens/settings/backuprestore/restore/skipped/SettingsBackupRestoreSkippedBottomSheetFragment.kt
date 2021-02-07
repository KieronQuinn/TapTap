package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.skipped

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel

class SettingsBackupRestoreSkippedBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val arguments by navArgs<SettingsBackupRestoreSkippedBottomSheetFragmentArgs>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?) = materialDialog.apply {
        title(R.string.settings_backuprestore_restore_skipped_title)
        message(text = getMessage())
        withOk()
    }

    private fun getMessage(): CharSequence = SpannableStringBuilder().apply {
        appendLine(getString(R.string.settings_backuprestore_restore_skipped_content))
        appendLine()
        for(item in arguments.skippedItems){
            append(item.actionOrGate.wrapActionGate(), StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(": ")
            if(item.reason == SettingsBackupRestoreRestoreViewModel.SkippedReason.APP_NOT_INSTALLED){
                append(getString(item.reason.descriptionRes, item.actionOrGate.getAppPackageName()))
            }else{
                append(getString(item.reason.descriptionRes))
            }
            appendLine()
        }
    }.trim()

    private fun SettingsBackupRestoreRestoreViewModel.ActionGate.getAppPackageName(): String {
        return when(this){
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Action -> this.action.data!!
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> this.gate.data!!
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> this.gate.data!!
        }
    }

    private fun SettingsBackupRestoreRestoreViewModel.ActionGate.wrapActionGate(): CharSequence {
        return when(this){
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Action -> getString(R.string.settings_backuprestore_restore_skipped_action, getString(this.action.action.nameRes))
            is SettingsBackupRestoreRestoreViewModel.ActionGate.Gate -> getString(R.string.settings_backuprestore_restore_skipped_gate, getString(this.gate.gate.nameRes))
            is SettingsBackupRestoreRestoreViewModel.ActionGate.WhenGate -> getString(R.string.settings_backuprestore_restore_skipped_action_requirement, getString(this.gate.gate.nameRes), getString(this.parentAction.action.nameRes))
        }
    }

}