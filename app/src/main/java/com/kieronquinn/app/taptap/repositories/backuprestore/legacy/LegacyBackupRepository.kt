package com.kieronquinn.app.taptap.repositories.backuprestore.legacy

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.models.backup.LegacyBackup
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

interface LegacyBackupRepository {

    fun getBackupJson(context: Context): String

}

class LegacyBackupRepositoryImpl(private val gson: Gson): LegacyBackupRepository {

    companion object {
        private const val BACKUP_JSON_KEY_DOUBLE = "double"
        private const val BACKUP_JSON_KEY_TRIPLE = "triple"
        private const val BACKUP_JSON_KEY_GATES = "gates"
        private const val BACKUP_JSON_KEY_SETTINGS = "settings"
        private const val BACKUP_JSON_KEY_SETTINGS_LEGACY = "settings_legacy"
    }

    private fun getDoubleTapActionListFile(context: Context): File {
        return File(context.filesDir, "actions.json")
    }

    private fun getTripleTapActionListFile(context: Context): File {
        return File(context.filesDir, "actions_triple.json")
    }

    private fun getGateListFile(context: Context): File {
        return File(context.filesDir, "gates.json")
    }

    private fun getActionsFromFile(file: File): Array<LegacyBackup.Action> {
        val defaults = emptyList<LegacyBackup.Action>()
        if(!file.exists()) return defaults.toTypedArray()
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return defaults.toTypedArray()
        return gson.fromJson(fileData, Array<LegacyBackup.Action>::class.java)
    }

    private fun getGatesFromFile(file: File): Array<LegacyBackup.Gate> {
        if(!file.exists()) return emptyArray()
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return emptyArray()
        return gson.fromJson(fileData, Array<LegacyBackup.Gate>::class.java).mapNotNull {
            if(it.name == null) null else it
        }.toTypedArray()
    }

    override fun getBackupJson(context: Context): String {
        val doubleTapActions = getActionsFromFile(getDoubleTapActionListFile(context))
        val tripleTapActions = getActionsFromFile(getTripleTapActionListFile(context))
        val gates = getGatesFromFile(getGateListFile(context))
        val settings = getSettingsAsJson(context)
        val legacySettings = getLegacySettingsAsJson(context)
        return JSONObject().apply {
            put(BACKUP_JSON_KEY_DOUBLE, gson.toJson(doubleTapActions))
            put(BACKUP_JSON_KEY_TRIPLE, gson.toJson(tripleTapActions))
            put(BACKUP_JSON_KEY_GATES, gson.toJson(gates))
            put(BACKUP_JSON_KEY_SETTINGS, settings)
            put(BACKUP_JSON_KEY_SETTINGS_LEGACY, legacySettings)
        }.toString()
    }

    private fun getSettingsAsJson(context: Context) = JSONArray().apply {
        context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE).toJson(this)
    }

    private fun getLegacySettingsAsJson(context: Context) = JSONArray().apply {
        context.getSharedPreferences( "${BuildConfig.APPLICATION_ID}_preferences", Context.MODE_PRIVATE).toJson(this)
    }

    private fun SharedPreferences.toJson(jsonArray: JSONArray) = jsonArray.run {
        all.map {
            when(it.value){
                is Boolean -> {
                    JSONObject().fromSetting(it.key, it.value as Boolean, SettingsJsonType.BOOLEAN)
                }
                is Int -> {
                    JSONObject().fromSetting(it.key, it.value as Int, SettingsJsonType.INT)
                }
                is Float -> {
                    JSONObject().fromSetting(it.key, it.value.toString(), SettingsJsonType.FLOAT)
                }
                is Long -> {
                    JSONObject().fromSetting(it.key, it.value as Long, SettingsJsonType.LONG)
                }
                is String -> {
                    JSONObject().fromSetting(it.key, it.value as String, SettingsJsonType.STRING)
                }
                else -> null
            }
        }.forEach {
            if(it != null) put(it)
        }
    }

    private fun <T> JSONObject.fromSetting(key: String, value: T, type: SettingsJsonType): JSONObject {
        put("key", key)
        put("value", value)
        put("type", type)
        return this
    }

    private enum class SettingsJsonType {
        STRING, BOOLEAN, INT, FLOAT, LONG
    }

}