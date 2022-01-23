package com.google.android.columbus

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log

class ColumbusSettings(
    context: Context,
    contentObserverFactory: ColumbusContentObserverFactory
) {

    interface ColumbusSettingsChangeListener {

        fun onAlertSilenceEnabledChange(enabled: Boolean) {
            //Stub
        }

        fun onColumbusEnabledChange(enabled: Boolean) {
            //Stub
        }

        fun onLowSensitivityChange(enabled: Boolean) {
            //Stub
        }

        fun onSelectedActionChange(actionId: String) {
            //Stub
        }

        fun onSelectedAppChange(app: String) {
            //Stub
        }

        fun onSelectedAppShortcutChange(shortcut: String) {
            //Stub
        }

        fun onUseApSensorChange(enabled: Boolean) {
            //Stub
        }

    }

    companion object {
        private const val TAG = "Columbus/Settings"
        private const val KEY_COLUMBUS_ENABLED = "columbus_enabled"
        private const val KEY_AP_SENSOR = "columbus_ap_sensor"
        private const val KEY_ACTION = "columbus_action"
        private const val KEY_LAUNCH_APP = "columbus_launch_app"
        private const val KEY_LAUNCH_APP_SHORTCUT = "columbus_launch_app_shortcut"
        private const val KEY_LOW_SENSITIVITY = "columbus_low_sensitivity"
        private const val KEY_SILENCE_ALERTS = "columbus_silence_alerts"
        val COLUMBUS_ENABLED_URI = Settings.Secure.getUriFor(KEY_COLUMBUS_ENABLED)
        val COLUMBUS_AP_SENSOR_URI = Settings.Secure.getUriFor(KEY_AP_SENSOR)
        val COLUMBUS_ACTION_URI = Settings.Secure.getUriFor(KEY_ACTION)
        val COLUMBUS_LAUNCH_APP_URI = Settings.Secure.getUriFor(KEY_LAUNCH_APP)
        val COLUMBUS_LAUNCH_APP_SHORTCUT_URI = Settings.Secure.getUriFor(KEY_LAUNCH_APP_SHORTCUT)
        val COLUMBUS_LOW_SENSITIVITY_URI = Settings.Secure.getUriFor(KEY_LOW_SENSITIVITY)
        val COLUMBUS_SILENCE_ALERTS_URI = Settings.Secure.getUriFor(KEY_SILENCE_ALERTS)
        private val MONITORED_URIS = setOf(
            COLUMBUS_ENABLED_URI,
            COLUMBUS_AP_SENSOR_URI,
            COLUMBUS_ACTION_URI,
            COLUMBUS_LAUNCH_APP_URI,
            COLUMBUS_LAUNCH_APP_SHORTCUT_URI,
            COLUMBUS_LOW_SENSITIVITY_URI,
            COLUMBUS_SILENCE_ALERTS_URI
        )
    }

    private val contentResolver = context.contentResolver
    private val listeners = LinkedHashSet<ColumbusSettingsChangeListener>()
    private var registered = false

    private fun callback(uri: Uri?){
        when(uri){
            COLUMBUS_ENABLED_URI -> {
                listeners.forEach { it.onColumbusEnabledChange(isColumbusEnabled) }
            }
            COLUMBUS_AP_SENSOR_URI -> {
                listeners.forEach { it.onUseApSensorChange(useApSensor) }
            }
            COLUMBUS_ACTION_URI -> {
                listeners.forEach { it.onSelectedActionChange(selectedAction) }
            }
            COLUMBUS_LAUNCH_APP_URI -> {
                listeners.forEach { it.onSelectedAppChange(selectedApp) }
            }
            COLUMBUS_LAUNCH_APP_SHORTCUT_URI -> {
                listeners.forEach { it.onSelectedAppShortcutChange(selectedAppShortcut) }
            }
            COLUMBUS_LOW_SENSITIVITY_URI -> {
                listeners.forEach { it.onLowSensitivityChange(useLowSensitivity) }
            }
            COLUMBUS_SILENCE_ALERTS_URI -> {
                listeners.forEach { it.onAlertSilenceEnabledChange(silenceAlertsEnabled) }
            }
            else -> {
                Log.w(TAG, "Unknown setting change: $uri")
            }
        }
    }

    private val contentObservers = MONITORED_URIS.map {
        contentObserverFactory.create(it, this::callback)
    }

    val isColumbusEnabled
        get() = true //Settings.Secure.getInt(contentResolver, KEY_COLUMBUS_ENABLED, 0) != 0

    val selectedAction
        get() = "" //Settings.Secure.getString(contentResolver, KEY_ACTION)

    val selectedApp
        get() = "" //Settings.Secure.getString(contentResolver, KEY_LAUNCH_APP)

    val selectedAppShortcut
        get() = "" //Settings.Secure.getString(contentResolver, KEY_LAUNCH_APP_SHORTCUT)

    val silenceAlertsEnabled
        get() = false //Settings.Secure.getInt(contentResolver, KEY_SILENCE_ALERTS, 1) != 0

    val useApSensor
        get() = false //Settings.Secure.getInt(contentResolver, KEY_AP_SENSOR, 0) != 0

    val useLowSensitivity
        get() = false //Settings.Secure.getInt(contentResolver, KEY_LOW_SENSITIVITY, 0) != 0

    fun registerColumbusSettingsChangeListener(listener: ColumbusSettingsChangeListener){
        listeners.add(listener)
        if (!registered && listeners.isNotEmpty()) {
            registered = true
            contentObservers.forEach {
                it.activate()
            }
        }
    }

    fun unregisterColumbusSettingsChangeListener(listener: ColumbusSettingsChangeListener){
        listeners.remove(listener)
        if(registered && listeners.isEmpty()){
            registered = false
            contentObservers.forEach {
                it.deactivate()
            }
        }
    }

}