package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.repositories.backuprestore.BackupRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsBackupRestoreBackupViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun setBackupUri(uri: Uri)
    abstract fun onCloseClicked()

    sealed class State {
        object BackingUp: State()
        data class Finished(val result: BackupRepository.BackupResult): State()
    }

}

class SettingsBackupRestoreBackupViewModelImpl(context: Context, backupRepository: BackupRepository, private val navigation: ContainerNavigation): SettingsBackupRestoreBackupViewModel() {

    private val uri = MutableSharedFlow<Uri>()

    override val state = uri.take(1).map {
        State.Finished(backupRepository.backupToUri(context, it))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.BackingUp)

    override fun setBackupUri(uri: Uri) {
        viewModelScope.launch {
            this@SettingsBackupRestoreBackupViewModelImpl.uri.emit(uri)
        }
    }

    override fun onCloseClicked() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}