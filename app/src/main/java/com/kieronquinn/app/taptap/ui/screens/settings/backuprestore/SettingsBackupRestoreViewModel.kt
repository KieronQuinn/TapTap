package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class SettingsBackupRestoreViewModel: ViewModel() {

    abstract fun onBackupClicked(launcher: ActivityResultLauncher<String>)
    abstract fun onRestoreClicked(launcher: ActivityResultLauncher<Array<String?>>)

    abstract fun onBackupFileClicked(uri: Uri)
    abstract fun onRestoreFileClicked(uri: Uri)

}

class SettingsBackupRestoreViewModelImpl(private val navigation: ContainerNavigation): SettingsBackupRestoreViewModel() {

    companion object {
        const val TAP_TAP_BACKUP_FILE_TEMPLATE = "backup_%s.taptap"
        private val TAP_TAP_BACKUP_MIME_TYPE = MimeTypeMap.getSingleton().getMimeTypeFromExtension("taptap")
    }

    override fun onBackupClicked(launcher: ActivityResultLauncher<String>) {
        launcher.launch(getFilename())
    }

    override fun onBackupFileClicked(uri: Uri) {
        viewModelScope.launch {
            navigation.navigate(SettingsBackupRestoreFragmentDirections.actionSettingsBackupRestoreFragmentToSettingsBackupRestoreBackupFragment(uri))
        }
    }

    override fun onRestoreClicked(launcher: ActivityResultLauncher<Array<String?>>) {
        launcher.launch(arrayOf(TAP_TAP_BACKUP_MIME_TYPE))
    }

    override fun onRestoreFileClicked(uri: Uri) {
        viewModelScope.launch {
            navigation.navigate(SettingsBackupRestoreFragmentDirections.actionSettingsBackupRestoreFragmentToSettingsBackupRestoreRestoreFragment(uri))
        }
    }

    private fun getFilename(): String {
        val time = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return String.format(TAP_TAP_BACKUP_FILE_TEMPLATE, dateTimeFormatter.format(time))
    }

}