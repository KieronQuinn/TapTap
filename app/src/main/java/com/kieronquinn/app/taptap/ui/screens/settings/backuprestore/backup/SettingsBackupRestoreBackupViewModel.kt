package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.utils.extensions.gzip
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SettingsBackupRestoreBackupViewModel(private val tapFileRepository: TapFileRepository): BaseViewModel() {

    val state = MutableStateFlow<State>(State.Running)

    fun getTitle(context: Context): Flow<String> = flow {
        state.collect {
            emit(when(it){
                is State.Running -> context.getString(R.string.settings_backuprestore_backup_title)
                is State.Cancelled -> context.getString(R.string.settings_backuprestore_backup_title)
                is State.Done -> context.getString(R.string.settings_backuprestore_backup_done_title)
                is State.Error -> context.getString(R.string.settings_backuprestore_backup_error_title)
            })
        }
    }

    fun getContent(context: Context): Flow<String> = flow {
        state.collect {
            emit(when(it){
                is State.Running -> context.getString(R.string.settings_backuprestore_backup_desc)
                is State.Cancelled -> context.getString(R.string.settings_backuprestore_backup_desc)
                is State.Done -> context.getString(R.string.settings_backuprestore_backup_done_desc, it.fileName)
                is State.Error -> context.getString(R.string.settings_backuprestore_backup_error_desc, context.getString(it.errorType.errorRes))
            })
        }
    }

    fun createBackup(context: Context, backupUri: Uri) = viewModelScope.launch {
        runCatching {
            val file = DocumentFile.fromSingleUri(context, backupUri) ?: run {
                state.emit(State.Error(ErrorType.ERROR_CREATING_FILE))
                return@launch
            }
            val outputStream = context.contentResolver.openOutputStream(file.uri) ?: run {
                state.emit(State.Error(ErrorType.ERROR_WRITING_FILE))
                return@launch
            }
            outputStream.run {
                bufferedWriter().run {
                    write(getBackupString(context).gzip())
                    flush()
                    close()
                }
                flush()
                close()
            }
            //Artificial delay to give the indication that it's actually *done* something
            delay(2000L)
            file
        }.onFailure {
            state.emit(State.Error(ErrorType.UNKNOWN))
        }.onSuccess {
            state.emit(State.Done(it.name!!))
        }
    }

    private fun getBackupString(context: Context): String {
        return tapFileRepository.getBackupJson(context)
    }

    fun onCloseClicked(fragment: Fragment){
        fragment.findNavController().navigateUp()
    }

    sealed class State {
        object Running: State()
        data class Done(val fileName: String): State()
        data class Error(val errorType: ErrorType): State()
        object Cancelled: State()
    }

    fun cancel() = viewModelScope.launch {
        state.emit(State.Cancelled)
    }

    enum class ErrorType(@StringRes val errorRes: Int) {
        ERROR_CREATING_FILE(R.string.backup_restore_error_type_failed_create_file),
        ERROR_WRITING_FILE(R.string.backup_restore_error_type_failed_write_file),
        UNKNOWN(R.string.backup_restore_error_type_failed_unknown)
    }

}