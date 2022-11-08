package com.kieronquinn.app.taptap.repositories.backuprestore

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.components.columbus.sensors.TapTapGestureSensorImpl
import com.kieronquinn.app.taptap.components.settings.SETTINGS_VERSION
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.backup.Backup
import com.kieronquinn.app.taptap.models.backup.LegacyBackup
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.backuprestore.legacy.LegacyBackupRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.DoubleTapAction
import com.kieronquinn.app.taptap.repositories.room.actions.TripleTapAction
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateDouble
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateTriple
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import com.kieronquinn.app.taptap.utils.extensions.ungzip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

abstract class RestoreRepository {

    abstract suspend fun loadRestoreCandidate(context: Context, uri: Uri): RestoreCandidate?
    abstract suspend fun performRestore(candidate: RestoreCandidate, ignoreRequirements: Boolean = false): Boolean
    abstract suspend fun upgradeSettings(context: Context)
    abstract suspend fun shouldUpgrade(context: Context): Boolean

    data class RestoreAction(
        val uuid: String = UUID.randomUUID().toString(),
        val action: Backup.Action,
        var actionRequirements: Array<ActionRequirement>?,
        var supported: Boolean,
        var skipped: Boolean = false
    )

    data class RestoreGate(
        val uuid: String = UUID.randomUUID().toString(),
        val gate: Backup.Gate,
        var gateRequirements: Array<GateRequirement>?,
        var supported: Boolean,
        var skipped: Boolean = false
    )

    data class RestoreWhenGate(
        val uuid: String = UUID.randomUUID().toString(),
        val gate: Backup.WhenGate,
        var gateRequirements: Array<GateRequirement>?,
        var supported: Boolean,
        var skipped: Boolean = false
    )

    data class RestoreCandidate(
        val filename: String,
        val settings: Backup.Settings,
        val doubleTapActions: List<RestoreAction>,
        val tripleTapActions: List<RestoreAction>,
        val gates: List<RestoreGate>,
        val whenGatesDouble: List<RestoreWhenGate>,
        val whenGatesTriple: List<RestoreWhenGate>
    )

}

class RestoreRepositoryImpl(
    private val actionsRepository: ActionsRepository,
    private val gatesRepository: GatesRepository,
    private val whenGatesRepositoryDouble: WhenGatesRepositoryDouble<*>,
    private val whenGatesRepositoryTriple: WhenGatesRepositoryTriple<*>,
    private val tapTapSettings: TapTapSettings,
    private val legacyBackupRepository: LegacyBackupRepository,
    private val gson: Gson
) : RestoreRepository() {

    companion object {
        private val LEGACY_FILES = arrayOf("actions.json", "actions_triple.json", "gates.json")
    }

    override suspend fun loadRestoreCandidate(context: Context, uri: Uri): RestoreCandidate? =
        withContext(Dispatchers.IO) {
            try {
                val filename = DocumentFile.fromSingleUri(context, uri)?.name ?: return@withContext null
                val fileInput =
                    context.contentResolver.openInputStream(uri) ?: return@withContext null
                val rawJson = fileInput.use { it.readBytes().ungzip() }
                val backup = loadBackup(rawJson) ?: loadLegacyBackup(rawJson) ?: return@withContext null
                if(backup.metadata?.backupVersion != Backup.BACKUP_VERSION) return@withContext null //Reject unknown backups
                createRestoreCandidate(context, filename, backup)
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun performRestore(candidate: RestoreCandidate, ignoreRequirements: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            val doubleTapActions = candidate.doubleTapActions.mapNotNull {
                if((it.skipped || !it.supported || it.actionRequirements?.isNotEmpty() == true) && !ignoreRequirements) return@mapNotNull null
                DoubleTapAction(
                    actionId = it.action.id ?: return@mapNotNull null,
                    name = it.action.name ?: return@mapNotNull null,
                    index = it.action.index ?: return@mapNotNull null,
                    extraData = it.action.extraData ?: ""
                )
            }
            actionsRepository.clearDoubleTapActions()
            delay(250L)
            doubleTapActions.forEach {
                actionsRepository.addDoubleTapAction(it)
            }
            val tripleTapActions = candidate.tripleTapActions.mapNotNull {
                if((it.skipped || !it.supported || it.actionRequirements?.isNotEmpty() == true) && !ignoreRequirements) return@mapNotNull null
                TripleTapAction(
                    actionId = it.action.id ?: return@mapNotNull null,
                    name = it.action.name ?: return@mapNotNull null,
                    index = it.action.index ?: return@mapNotNull null,
                    extraData = it.action.extraData ?: ""
                )
            }
            actionsRepository.clearTripleTapActions()
            delay(250L)
            tripleTapActions.forEach {
                actionsRepository.addTripleTapAction(it)
            }
            val gates = candidate.gates.mapNotNull {
                if((it.skipped || !it.supported || it.gateRequirements?.isNotEmpty() == true) && !ignoreRequirements) return@mapNotNull null
                Gate(
                    gateId = it.gate.id ?: return@mapNotNull null,
                    name = it.gate.name ?: return@mapNotNull null,
                    enabled = it.gate.enabled ?: return@mapNotNull null,
                    index = it.gate.index ?: return@mapNotNull null,
                    extraData = it.gate.extraData ?: ""
                )
            }
            gatesRepository.clearAll()
            delay(250L)
            gates.forEach {
                gatesRepository.addGate(it)
            }
            val whenGatesDouble = candidate.whenGatesDouble.mapNotNull {
                if((it.skipped || !it.supported || it.gateRequirements?.isNotEmpty() == true) && !ignoreRequirements) return@mapNotNull null
                WhenGateDouble(
                    whenGateId = it.gate.id ?: return@mapNotNull null,
                    actionId = it.gate.actionId ?: return@mapNotNull null,
                    name = it.gate.name ?: return@mapNotNull null,
                    invert = it.gate.invert ?: return@mapNotNull null,
                    index = it.gate.index ?: return@mapNotNull null,
                    extraData = it.gate.extraData ?: ""
                )
            }
            whenGatesRepositoryDouble.clearAll()
            delay(250L)
            whenGatesDouble.forEach {
                whenGatesRepositoryDouble.addWhenGate(it)
            }
            val whenGatesTriple = candidate.whenGatesTriple.mapNotNull {
                if((it.skipped || !it.supported || it.gateRequirements?.isNotEmpty() == true) && !ignoreRequirements) return@mapNotNull null
                WhenGateTriple(
                    whenGateId = it.gate.id ?: return@mapNotNull null,
                    actionId = it.gate.actionId ?: return@mapNotNull null,
                    name = it.gate.name ?: return@mapNotNull null,
                    invert = it.gate.invert ?: return@mapNotNull null,
                    index = it.gate.index ?: return@mapNotNull null,
                    extraData = it.gate.extraData ?: ""
                )
            }
            whenGatesRepositoryTriple.clearAll()
            delay(250L)
            whenGatesTriple.forEach {
                whenGatesRepositoryTriple.addWhenGate(it)
            }
            tapTapSettings.restoreSettings(candidate.settings)
            true
        }

    private suspend fun createRestoreCandidate(context: Context, filename: String, backup: Backup): RestoreCandidate? {
        val doubleTapActions = backup.doubleTapActions?.mapNotNull {
            val action = TapTapActionDirectory.valueFor(it.name ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val requirements = if (!actionsRepository.isActionRequirementSatisfied(
                    context,
                    action
                )
            ) action.actionRequirement else null
            val supported = actionsRepository.isActionSupported(context, action)
            RestoreAction(action = it, actionRequirements = requirements, supported = supported)
        } ?: emptyList()
        val tripleTapActions = backup.tripleTapActions?.mapNotNull {
            val action = TapTapActionDirectory.valueFor(it.name ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val requirements = if (!actionsRepository.isActionRequirementSatisfied(
                    context,
                    action
                )
            ) action.actionRequirement else null
            val supported = actionsRepository.isActionSupported(context, action)
            RestoreAction(action = it, actionRequirements = requirements, supported = supported)
        } ?: emptyList()
        val gates = backup.gates?.mapNotNull {
            val gate = TapTapGateDirectory.valueFor(it.name ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val requirements = if (!gatesRepository.isGateRequirementSatisfied(
                    context,
                    gate
                )
            ) gate.gateRequirement else null
            val supported = gatesRepository.isGateSupported(context, gate)
            RestoreGate(gate = it, gateRequirements = requirements, supported = supported)
        } ?: emptyList()
        val whenGatesDouble = backup.whenGatesDouble?.mapNotNull {
            val gate = TapTapGateDirectory.valueFor(it.name ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val requirements = if (!gatesRepository.isGateRequirementSatisfied(
                    context,
                    gate
                )
            ) gate.gateRequirement else null
            val supported = gatesRepository.isGateSupported(context, gate)
            RestoreWhenGate(gate = it, gateRequirements = requirements, supported = supported)
        } ?: emptyList()
        val whenGatesTriple = backup.whenGatesTriple?.mapNotNull {
            val gate = TapTapGateDirectory.valueFor(it.name ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val requirements = if (!gatesRepository.isGateRequirementSatisfied(
                    context,
                    gate
                )
            ) gate.gateRequirement else null
            val supported = gatesRepository.isGateSupported(context, gate)
            RestoreWhenGate(gate = it, gateRequirements = requirements, supported = supported)
        } ?: emptyList()
        return RestoreCandidate(
            filename,
            backup.settings ?: return null,
            doubleTapActions,
            tripleTapActions,
            gates,
            whenGatesDouble,
            whenGatesTriple
        )
    }

    private fun loadBackup(rawJson: String): Backup? {
        return try {
            gson.fromJson(rawJson, Backup::class.java)
        }catch (e: JsonParseException) {
            null
        }
    }

    private fun loadLegacyBackup(rawJson: String): Backup? {
        return try {
            val legacyBackup = gson.fromJson(rawJson, LegacyBackup::class.java)
            //Convert format
            val backup = Backup()
            val doubleTapActions = legacyBackup.getDoubleTapActions(gson)
            val tripleTapActions = legacyBackup.getTripleTapActions(gson)
            backup.doubleTapActions = doubleTapActions?.convertLegacyBackupActions()
            backup.tripleTapActions = tripleTapActions?.convertLegacyBackupActions()
            backup.gates = legacyBackup.getGates(gson)?.convertLegacyBackupGates()
            backup.whenGatesDouble = doubleTapActions?.convertLegacyBackupWhenGates() ?: emptyList()
            backup.whenGatesTriple = tripleTapActions?.convertLegacyBackupWhenGates() ?: emptyList()
            val settings = listOfNotNull(legacyBackup.settings, legacyBackup.legacySettings).flatten()
            backup.settings = settings.convertLegacySettings()
            backup.metadata = Backup.Metadata()
            return backup
        }catch (e: JsonParseException) {
            null
        }
    }

    private fun Array<LegacyBackup.Action>.convertLegacyBackupActions(): List<Backup.Action> {
        return mapIndexedNotNull { index, action ->
            Backup.Action().apply {
                this.id = index
                this.name = action.name ?: return@mapIndexedNotNull null
                this.index = index
                this.extraData = action.extraData
            }
        }
    }

    private fun Array<LegacyBackup.Gate>.convertLegacyBackupGates(): List<Backup.Gate> {
        return mapIndexedNotNull { index, gate ->
            Backup.Gate().apply {
                this.id = index
                this.name = gate.name ?: return@mapIndexedNotNull null
                this.index = index
                this.enabled = gate.isActivated
                this.extraData = gate.extraData
            }
        }
    }

    private fun Array<LegacyBackup.Action>.convertLegacyBackupWhenGates(): List<Backup.WhenGate> {
        return mapIndexedNotNull { index, action ->
            if(action.whenGates.isNullOrEmpty()) return@mapIndexedNotNull null
            Pair(index, action.whenGates)
        }.flatMapIndexed { index: Int, item ->
            item.second.map {
                Backup.WhenGate().apply {
                    this.id = index
                    this.index = index
                    this.actionId = item.first
                    this.name = it.name
                    this.extraData = it.extraData
                    this.invert = it.isInverted
                }
            }
        }
    }

    private fun List<LegacyBackup.Setting>.convertLegacySettings(): Backup.Settings {
        return Backup.Settings().apply {
            forEach {
                when(it.key){
                    "main_enabled" -> serviceEnabled = it.value?.toBooleanStrictOrNull()
                    "triple_tap_enabled" -> actionsTripleTapEnabled = it.value?.toBooleanStrictOrNull()
                    "model" -> columbusTapModel = TapModel.values().firstOrNull { model -> model.name == it.value }?.name
                    "feedback_vibrate" -> feedbackVibrate = it.value?.toBooleanStrictOrNull()
                    "feedback_wake" -> feedbackWakeDevice = it.value?.toBooleanStrictOrNull()
                    "feedback_override_dnd" -> feedbackVibrateDND = it.value?.toBooleanStrictOrNull()
                    "advanced_restart_service" -> advancedAutoRestart = it.value?.toBooleanStrictOrNull()
                    "sensitivity" -> {
                        val value = it.value?.toFloatOrNull() ?: return@forEach
                        val presetSensitivity = TapTapGestureSensorImpl.COLUMBUS_SENSITIVITY_VALUES.indexOf(value)
                        if(presetSensitivity == -1){
                            columbusCustomSensitivity = value
                        }else{
                            columbusSensitivityLevel = presetSensitivity
                        }
                    }
                }
            }
        }
    }

    private suspend fun TapTapSettings.restoreSettings(settings: Backup.Settings) {
        settings.serviceEnabled?.let {
            serviceEnabled.set(it)
        }
        settings.lowPowerMode?.let {
            lowPowerMode.set(it)
        }
        settings.columbusCHRELowSensitivity?.let {
            columbusCHRELowSensitivity.set(it)
        }
        settings.columbusSensitivityLevel?.let {
            columbusSensitivityLevel.set(it)
        }
        settings.columbusTapModel?.let { name ->
            columbusTapModel.set(TapModel.values().firstOrNull { it.name == name } ?: return@let)
        }
        settings.reachabilityLeftHanded?.let {
            reachabilityLeftHanded.set(it)
        }
        settings.feedbackVibrate?.let {
            feedbackVibrate.set(it)
        }
        settings.feedbackVibrateDND?.let {
            feedbackVibrateDND.set(it)
        }
        settings.feedbackWakeDevice?.let {
            feedbackWakeDevice.set(it)
        }
        settings.advancedLegacyWake?.let {
            advancedLegacyWake.set(it)
        }
        settings.advancedAutoRestart?.let {
            advancedAutoRestart.set(it)
        }
        settings.advancedTensorLowPower?.let {
            advancedTensorLowPower.set(it)
        }
        settings.actionsTripleTapEnabled?.let {
            actionsTripleTapEnabled.set(it)
        }
    }

    override suspend fun shouldUpgrade(context: Context): Boolean {
        return LEGACY_FILES.any { File(context.filesDir, it).exists() }
    }

    override suspend fun upgradeSettings(context: Context) = withContext(Dispatchers.IO) {
        val legacyBackupJson = legacyBackupRepository.getBackupJson(context)
        LEGACY_FILES.forEach { File(context.filesDir, it).delete() }
        val sharedPrefs = context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)
        val legacySharedPrefs = context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_preferences", Context.MODE_PRIVATE)
        legacySharedPrefs.edit().clear().commit()
        sharedPrefs.edit().clear().commit()
        //Create database tables by getting the default values
        actionsRepository.getSavedDoubleTapActions()
        actionsRepository.getSavedTripleTapActions()
        gatesRepository.getSavedGates()
        whenGatesRepositoryDouble.getWhenGates()
        whenGatesRepositoryTriple.getWhenGates()
        val legacyBackup = loadLegacyBackup(legacyBackupJson)
        val restoreCandidate = createRestoreCandidate(context, "legacy", legacyBackup ?: return@withContext)
        performRestore(restoreCandidate ?: return@withContext, true)
        //Allow time for sync
        delay(1000L)
        tapTapSettings.settingsVersion.set(SETTINGS_VERSION)
        tapTapSettings.hasSeenSetup.set(true)
    }

}
