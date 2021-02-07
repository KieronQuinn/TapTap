package com.kieronquinn.app.taptap.core

import android.content.Context
import android.content.SharedPreferences
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.tfcporciuncula.flow.FlowSharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class TapSharedPreferences(private val context: Context) {

    companion object {
        const val SHARED_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}_prefs"
        const val SHARED_PREFERENCES_NAME_LEGACY = "${BuildConfig.APPLICATION_ID}_preferences"
        const val SHARED_PREFERENCES_KEY_MAIN_SWITCH = "main_enabled"
        const val SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH = "triple_tap_enabled"
        const val SHARED_PREFERENCES_KEY_MODEL = "model"
        const val SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE = "feedback_vibrate"
        const val SHARED_PREFERENCES_KEY_FEEDBACK_WAKE = "feedback_wake"
        const val SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND = "feedback_override_dnd"
        const val SHARED_PREFERENCES_KEY_SPLIT_SERVICE = "advanced_split_service"
        const val SHARED_PREFERENCES_KEY_RESTART_SERVICE = "advanced_restart_service"
        const val SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP = "has_seen_setup"
        const val SHARED_PREFERENCES_KEY_BACKUP_URI = "backup_uri"

        //Legacy preferences used in previous versions to know when actions have been set. This was previously used to know if the user had used the app.
        const val SHARED_PREFERENCES_KEY_ACTIONS_TIME = "actions_time"
        const val SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME = "actions_triple_time"

        const val SHARED_PREFERENCES_KEY_SENSITIVITY = "sensitivity"

        /*
            SENSITIVITY
            These values get applied to the model's noise reduction. The higher the value, the more reduction of 'noise', and therefore the harder the gesture is to run.
            Anything from 0.0 to 0.1 should really work, but 0.75 is pretty hard to trigger so that's set to the maximum and values filled in from there
            For > 0.05f, the values were initially even spaced, but that put too much weight on the higher values which made the force difference between 0.05 (default) the next value too great
            Instead I made up some values that are semi-evenly spaced and seem to provide a decent weighting
            For < 0.05f, the values are evenly spaced down to 0 which is no noise removal at all and really easy to trigger.
         */
        val SENSITIVITY_VALUES = arrayOf(0.75f, 0.53f, 0.40f, 0.25f, 0.1f, 0.05f, 0.04f, 0.03f, 0.02f, 0.01f, 0.0f)
    }

    val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    val sharedPreferencesLegacy = context.getSharedPreferences(SHARED_PREFERENCES_NAME_LEGACY, Context.MODE_PRIVATE)
    val flowSharedPreferences = FlowSharedPreferences(sharedPreferences)

    var isSplitService: Boolean
        get() = sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_SPLIT_SERVICE, true) ?: true
        set(value) = sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_SPLIT_SERVICE, value)?.apply() ?: Unit

    var isMainEnabled: Boolean
        get() = sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, true) ?: true
        set(value) = sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, value)?.apply() ?: Unit

    var isTripleTapEnabled: Boolean
        get() = sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH, false) ?: false
        set(value) = sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH, value)?.apply() ?: Unit

    var isRestartEnabled: Boolean
        get() = sharedPreferencesLegacy?.getBoolean(SHARED_PREFERENCES_KEY_RESTART_SERVICE, false) ?: false
        set(value) = sharedPreferencesLegacy?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_RESTART_SERVICE, value)?.apply() ?: Unit

    var hasSeenSetup: Boolean
        get() {
            if(sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP, false) == true) return true
            if(sharedPreferences?.contains(SHARED_PREFERENCES_KEY_ACTIONS_TIME) == true) return true
            if(sharedPreferences?.contains(SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME) == true) return true
            return false
        }
        set(value) {
            sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP, value)?.apply()
        }

    var model
        get() = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_MODEL, context.getDefaultTfModel().name) ?: context.getDefaultTfModel().name
        set(value) = sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_MODEL, value)?.apply() ?: Unit

    var sensitivity
        get() = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.toFloatOrNull() ?: 0.05f
        set(value) = sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_SENSITIVITY, value.toString())?.apply() ?: Unit

    var overrideDnd
        get() = sharedPreferencesLegacy?.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND, false) ?: false
        set(value) = sharedPreferencesLegacy?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND, value)?.apply() ?: Unit

    var backupUri
        get() = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_BACKUP_URI, "") ?: ""
        set(value) = sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_BACKUP_URI, value)?.apply() ?: Unit

    fun getSettingsAsJson() = JSONArray().apply {
        sharedPreferences.toJson(this)
    }

    fun getLegacySettingsAsJson() = JSONArray().apply {
        sharedPreferencesLegacy.toJson(this)
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

    fun writeFromJson(jsonArray: JSONArray) = sharedPreferences.edit().run {
        for(i in 0 until jsonArray.length()){
            putFromSettingJsonObject(jsonArray.getJSONObject(i))
        }
        apply()
    }

    fun writeFromJsonLegacy(jsonArray: JSONArray) = sharedPreferencesLegacy.edit().run {
        for(i in 0 until jsonArray.length()){
            putFromSettingJsonObject(jsonArray.getJSONObject(i))
        }
        apply()
    }

    private fun SharedPreferences.Editor.putFromSettingJsonObject(jsonObject: JSONObject){
        runCatching {
            val key = jsonObject.getString("key")
            when (SettingsJsonType.valueOf(jsonObject.getString("type"))) {
                SettingsJsonType.STRING -> putString(key, jsonObject.getString("value"))
                SettingsJsonType.BOOLEAN -> putBoolean(key, jsonObject.getBoolean("value"))
                SettingsJsonType.INT -> putInt(key, jsonObject.getInt("value"))
                SettingsJsonType.FLOAT -> putFloat(key, jsonObject.getString("value").toFloat())
                SettingsJsonType.LONG -> putLong(key, jsonObject.getLong("value"))
            }
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