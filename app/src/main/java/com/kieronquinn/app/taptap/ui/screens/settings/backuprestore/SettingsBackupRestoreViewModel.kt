package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SettingsBackupRestoreViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    companion object {
        const val TAP_BACKUP_FILE_TEMPLATE = "backup_%s.taptap"
    }

    fun onBackupClicked(fragment: SettingsBackupRestoreFragment){
        fragment.backupPickerResult.launch(getFilename())
    }

    fun onRestoreClicked(fragment: SettingsBackupRestoreFragment){
        fragment.restorePickerResult.launch(
            arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("taptap"))
        )
    }

    fun onBackupLocationPicked(fragment: Fragment, uri: Uri){
        val takeFlags: Int = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        fragment.requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
        tapSharedPreferences.backupUri = uri.toString()
        fragment.navigate(SettingsBackupRestoreFragmentDirections.actionSettingsBackupRestoreFragmentToSettingsBackupRestoreBackupFragment(uri))
    }

    fun onRestoreFilePicked(fragment: Fragment, uri: Uri){
        fragment.navigate(SettingsBackupRestoreFragmentDirections.actionSettingsBackupRestoreFragmentToSettingsBackupRestoreRestoreFragment(uri))
    }

    private fun getFilename(): String {
        val time = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return String.format(TAP_BACKUP_FILE_TEMPLATE, dateTimeFormatter.format(time))
    }

}