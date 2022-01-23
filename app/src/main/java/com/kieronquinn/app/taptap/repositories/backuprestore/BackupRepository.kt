package com.kieronquinn.app.taptap.repositories.backuprestore

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.backup.Backup
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.Action
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGate
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import com.kieronquinn.app.taptap.utils.extensions.gzip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BackupRepository {

    sealed class BackupResult {
        data class Success(val file: DocumentFile): BackupResult()
        data class Error(val errorType: ErrorType): BackupResult() {
            enum class ErrorType(@StringRes val errorRes: Int) {
                ERROR_CREATING_FILE(R.string.backup_restore_error_type_failed_create_file),
                ERROR_WRITING_FILE(R.string.backup_restore_error_type_failed_write_file),
                UNKNOWN(R.string.backup_restore_error_type_failed_unknown)
            }
        }
    }

    abstract suspend fun backupToUri(context: Context, uri: Uri): BackupResult

}

class BackupRepositoryImpl(
    private val actionsRepository: ActionsRepository,
    private val gatesRepository: GatesRepository,
    private val whenGatesRepositoryDouble: WhenGatesRepositoryDouble<*>,
    private val whenGatesRepositoryTriple: WhenGatesRepositoryTriple<*>,
    private val settings: TapTapSettings,
    private val gson: Gson
) : BackupRepository() {

    override suspend fun backupToUri(context: Context, uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val file = DocumentFile.fromSingleUri(context, uri) ?: run {
                return@withContext BackupResult.Error(BackupResult.Error.ErrorType.ERROR_CREATING_FILE)
            }
            val outputStream = context.contentResolver.openOutputStream(file.uri) ?: run {
                return@withContext BackupResult.Error(BackupResult.Error.ErrorType.ERROR_WRITING_FILE)
            }
            val backup = createBackup()
            val jsonBackup = gson.toJson(backup).gzip()
            outputStream.use {
                it.write(jsonBackup)
                it.flush()
            }
            BackupResult.Success(file)
        }catch (e: Exception){
            BackupResult.Error(BackupResult.Error.ErrorType.UNKNOWN)
        }
    }

    private suspend fun createBackup(): Backup {
        val metadata = getMetadata()
        val settings = getSettings()
        val doubleTapActions = getDoubleTapActions()
        val tripleTapActions = getTripleTapActions()
        val gates = getGates()
        val whenGatesDouble = getWhenGatesDouble()
        val whenGatesTriple = getWhenGatesTriple()
        return Backup().apply {
            this.metadata = metadata
            this.settings = settings
            this.doubleTapActions = doubleTapActions
            this.tripleTapActions = tripleTapActions
            this.gates = gates
            this.whenGatesDouble = whenGatesDouble
            this.whenGatesTriple = whenGatesTriple
        }
    }

    private fun getMetadata(): Backup.Metadata {
        return Backup.Metadata().apply {
            tapTapVersion = BuildConfig.VERSION_CODE
            tapTapVersionName = BuildConfig.VERSION_NAME
        }
    }

    private suspend fun getSettings(): Backup.Settings {
        return Backup.Settings().apply {
            this.serviceEnabled = settings.serviceEnabled.getOrNull()
            this.lowPowerMode = settings.lowPowerMode.getOrNull()
            this.columbusCHRELowSensitivity = settings.columbusCHRELowSensitivity.getOrNull()
            this.columbusSensitivityLevel = settings.columbusSensitivityLevel.getOrNull()
            this.columbusCustomSensitivity = settings.columbusCustomSensitivity.getOrNull()
            this.columbusTapModel = settings.columbusTapModel.getOrNull()?.name
            this.reachabilityLeftHanded = settings.reachabilityLeftHanded.getOrNull()
            this.feedbackVibrate = settings.feedbackVibrate.getOrNull()
            this.feedbackVibrateDND = settings.feedbackVibrateDND.getOrNull()
            this.feedbackWakeDevice = settings.feedbackVibrate.getOrNull()
            this.advancedLegacyWake = settings.advancedLegacyWake.getOrNull()
            this.advancedAutoRestart = settings.advancedAutoRestart.getOrNull()
            this.advancedTensorLowPower = settings.advancedTensorLowPower.getOrNull()
            this.actionsTripleTapEnabled = settings.actionsTripleTapEnabled.getOrNull()
        }
    }

    private suspend fun getDoubleTapActions(): List<Backup.Action> {
        return actionsRepository.getSavedDoubleTapActions().map {
            it.toBackupAction()
        }
    }

    private suspend fun getTripleTapActions(): List<Backup.Action> {
        return actionsRepository.getSavedTripleTapActions().map {
            it.toBackupAction()
        }
    }

    private suspend fun getGates(): List<Backup.Gate> {
        return gatesRepository.getSavedGates().map {
            it.toBackupGate()
        }
    }

    private suspend fun getWhenGatesDouble(): List<Backup.WhenGate> {
        return whenGatesRepositoryDouble.getWhenGates().map {
            it.toBackupWhenGate()
        }
    }

    private suspend fun getWhenGatesTriple(): List<Backup.WhenGate> {
        return whenGatesRepositoryTriple.getWhenGates().map {
            it.toBackupWhenGate()
        }
    }

    private fun Action.toBackupAction(): Backup.Action {
        return Backup.Action().apply {
            id = this@toBackupAction.actionId
            name = this@toBackupAction.name
            index = this@toBackupAction.index
            extraData = this@toBackupAction.extraData
        }
    }

    private fun Gate.toBackupGate(): Backup.Gate {
        return Backup.Gate().apply {
            id = this@toBackupGate.gateId
            name = this@toBackupGate.name
            enabled = this@toBackupGate.enabled
            index = this@toBackupGate.index
            extraData = this@toBackupGate.extraData
        }
    }

    private fun WhenGate.toBackupWhenGate(): Backup.WhenGate {
        return Backup.WhenGate().apply {
            id = this@toBackupWhenGate.whenGateId
            actionId = this@toBackupWhenGate.actionId
            name = this@toBackupWhenGate.name
            index = this@toBackupWhenGate.index
            invert = this@toBackupWhenGate.invert
            extraData = this@toBackupWhenGate.extraData
        }
    }

}