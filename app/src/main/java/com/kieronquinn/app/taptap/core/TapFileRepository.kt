package com.kieronquinn.app.taptap.core

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

class TapFileRepository(private val context: Context, private val tapSharedPreferences: TapSharedPreferences) {

    companion object {

        private fun getDoubleTapActionListFile(context: Context): File {
            return File(context.filesDir, "actions.json")
        }

        private fun getTripleTapActionListFile(context: Context): File {
            return File(context.filesDir, "actions_triple.json")
        }

        private fun getGateListFile(context: Context): File {
            return File(context.filesDir, "gates.json")
        }

        private val defaultGates by lazy {
            ALL_NON_CONFIG_GATES.map {
                createGateInternalForGate(it, DEFAULT_GATES.contains(it))
            }.toTypedArray()
        }

        private fun createActionInternalForAction(action: TapAction): ActionInternal {
            return ActionInternal(action, ArrayList())
        }

        private fun createGateInternalForGate(gate: TapGate, isActivated: Boolean): GateInternal {
            return GateInternal(gate, isActivated)
        }

        private const val BACKUP_JSON_KEY_DOUBLE = "double"
        private const val BACKUP_JSON_KEY_TRIPLE = "triple"
        private const val BACKUP_JSON_KEY_GATES = "gates"
        private const val BACKUP_JSON_KEY_SETTINGS = "settings"
        private const val BACKUP_JSON_KEY_SETTINGS_LEGACY = "settings_legacy"

    }

    private val doubleTapFile by lazy {
        getDoubleTapActionListFile(context)
    }

    private val tripleTapFile by lazy {
        getTripleTapActionListFile(context)
    }

    private val gatesFile by lazy {
        getGateListFile(context)
    }

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _doubleTapActions: MutableStateFlow<Array<ActionInternal>> = MutableStateFlow(emptyArray())
    private val _tripleTapActions: MutableStateFlow<Array<ActionInternal>> = MutableStateFlow(emptyArray())
    private val _gates = MutableStateFlow<Array<GateInternal>>(emptyArray())

    val doubleTapActions = _doubleTapActions.asStateFlow()
    val tripleTapActions = _tripleTapActions.asStateFlow()
    val gates = _gates.asStateFlow()

    fun getDoubleTapActions() = scope.launch {
        withContext(Dispatchers.IO) {
            _doubleTapActions.emit(getActionsFromFile(doubleTapFile, false))
        }
    }

    fun getTripleTapActions() = scope.launch {
        withContext(Dispatchers.IO) {
            _tripleTapActions.emit(getActionsFromFile(tripleTapFile, true))
        }
    }

    fun getGates() = scope.launch {
        withContext(Dispatchers.IO) {
            _gates.emit(getGatesFromFile(gatesFile))
        }
    }

    fun saveDoubleTapActions(actions: Array<ActionInternal>) = scope.launch {
        withContext(Dispatchers.IO) {
            val json = Gson().toJson(actions)
            doubleTapFile.writeText(json, Charset.defaultCharset())
            getDoubleTapActions()
        }
    }

    fun saveTripleTapActions(actions: Array<ActionInternal>) = scope.launch {
        withContext(Dispatchers.IO) {
            val json = Gson().toJson(actions)
            tripleTapFile.writeText(json, Charset.defaultCharset())
            getTripleTapActions()
        }
    }

    fun saveGates(gates: Array<GateInternal>) = scope.launch {
        withContext(Dispatchers.IO) {
            val json = Gson().toJson(gates)
            gatesFile.writeText(json, Charset.defaultCharset())
            getGates()
        }
    }

    private fun getActionsFromFile(file: File, triple: Boolean): Array<ActionInternal> {
        val defaults = if(triple) DEFAULT_ACTIONS_TRIPLE else DEFAULT_ACTIONS
        if(!file.exists()) return defaults.map {
            createActionInternalForAction(it)
        }.toTypedArray()
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return defaults.map {
            createActionInternalForAction(
                it
            )
        }.toTypedArray()
        return Gson().fromJson(fileData, Array<ActionInternal>::class.java)
    }

    private fun getGatesFromFile(file: File): Array<GateInternal> {
        if(!file.exists()) return defaultGates
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return defaultGates
        return Gson().fromJson(fileData, Array<GateInternal>::class.java).mapNotNull { if(it.gate == null) null else it }.toTypedArray()
    }

    fun getBackupJson(context: Context): String {
        val gson = Gson()
        val doubleTapActions = getActionsFromFile(getDoubleTapActionListFile(context), false)
        val tripleTapActions = getActionsFromFile(getTripleTapActionListFile(context), true)
        val gates = getGatesFromFile(getGateListFile(context))
        val settings = tapSharedPreferences.getSettingsAsJson()
        val legacySettings = tapSharedPreferences.getLegacySettingsAsJson()
        return JSONObject().apply {
            put(BACKUP_JSON_KEY_DOUBLE, gson.toJson(doubleTapActions))
            put(BACKUP_JSON_KEY_TRIPLE, gson.toJson(tripleTapActions))
            put(BACKUP_JSON_KEY_GATES, gson.toJson(gates))
            put(BACKUP_JSON_KEY_SETTINGS, settings)
            put(BACKUP_JSON_KEY_SETTINGS_LEGACY, legacySettings)
        }.toString()
    }

    fun restoreBackupJson(backupJson: BackupJson, skippedActions: Array<ActionInternal>, skippedGates: Array<GateInternal>): Boolean = with(backupJson) {
        return runCatching {
            saveDoubleTapActions(doubleTapActions.mapNotNull {
                if(skippedActions.contains(it)) null
                else ActionInternal(it.action, ArrayList<WhenGateInternal>().apply { addAll(it.whenList.filterNot { skippedGates.contains(it.toGateInternal()) })}, it.data)
            }.toTypedArray())
            saveTripleTapActions(tripleTapActions.mapNotNull {
                if(skippedActions.contains(it)) null
                else ActionInternal(it.action, ArrayList<WhenGateInternal>().apply { addAll(it.whenList.filterNot { skippedGates.contains(it.toGateInternal()) })}, it.data)
            }.toTypedArray())
            saveGates(gates.filterNot { skippedGates.contains(it) }.toTypedArray())
            tapSharedPreferences.writeFromJson(settings)
            tapSharedPreferences.writeFromJsonLegacy(settingsLegacy)
        }.isSuccess
    }

    fun loadBackupJson(fileName: String, jsonString: String): BackupJson? {
        runCatching {
            val gson = Gson()
            val json = JSONObject(jsonString)
            val doubleTapActions = gson.fromJson(json.getString(BACKUP_JSON_KEY_DOUBLE), Array<ActionInternal>::class.java)
            val tripleTapActions = gson.fromJson(json.getString(BACKUP_JSON_KEY_TRIPLE), Array<ActionInternal>::class.java)
            val gates = gson.fromJson(json.getString(BACKUP_JSON_KEY_GATES), Array<GateInternal>::class.java)
            val settings = json.getJSONArray(BACKUP_JSON_KEY_SETTINGS)
            val settingsLegacy = json.getJSONArray(BACKUP_JSON_KEY_SETTINGS_LEGACY)
            BackupJson(fileName, doubleTapActions.toList(), tripleTapActions.toList(), gates.toList(), settings, settingsLegacy)
        }.onFailure {
            it.printStackTrace()
            throw it
        }.onSuccess {
            return it
        }
        return null
    }

    fun isGestureServiceRequired(context: Context): Boolean {
        return doubleTapActions.value.any { GESTURE_REQUIRING_ACTIONS.contains(it.action) } || tripleTapActions.value.any { GESTURE_REQUIRING_ACTIONS.contains(it.action) }
    }

    data class BackupJson(val fileName: String, val doubleTapActions: List<ActionInternal>, val tripleTapActions: List<ActionInternal>, val gates: List<GateInternal>, val settings: JSONArray, val settingsLegacy: JSONArray){

        fun getWhenGates(): List<Pair<WhenGateInternal, ActionInternal>>{
            val list = emptyList<Pair<WhenGateInternal, ActionInternal>>().toMutableList()
            for(action in doubleTapActions){
                list.addAll(action.whenList.map { Pair(it, action) })
            }
            for(action in tripleTapActions){
                list.addAll(action.whenList.map { Pair(it, action) })
            }
            return list
        }

    }

}