package com.kieronquinn.app.taptap.components.settings

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl.SettingsConverters.SHARED_BOOLEAN
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl.SettingsConverters.SHARED_COLOR
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl.SettingsConverters.SHARED_FLOAT
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl.SettingsConverters.SHARED_INT
import com.kieronquinn.app.taptap.utils.extensions.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val SETTINGS_VERSION: Int = 1

interface TapTapSettings {

    //Current version of settings
    val settingsVersion: TapTapSetting<Int>

    //Whether the user has upgraded from < v1.0
    val settingsUpgraded: TapTapSetting<Boolean>

    //The wallpaper colour to use (< Android 12)
    val monetColor: TapTapSetting<Int>

    //Whether the user has completed or skipped setup
    val hasSeenSetup: TapTapSetting<Boolean>

    //Whether to show crash notifications to the user to share the stack traces on GitHub or XDA
    val enableCrashReporting: TapTapSetting<Boolean>

    //Service enabled
    val serviceEnabled: TapTapSetting<Boolean>

    //Whether internet access is allowed at all
    val internetAllowed: TapTapSetting<Boolean>

    //Whether to check for updates in the background. Will be ignored if internet is not allowed.
    val backgroundUpdateCheck: TapTapSetting<Boolean>

    //CHRE enabled
    val lowPowerMode: TapTapSetting<Boolean>

    //CHRE low sensitivity mode
    val columbusCHRELowSensitivity: TapTapSetting<Boolean>

    //Sensor sensitivity level, 1 - 10
    val columbusSensitivityLevel: TapTapSetting<Int>

    //Custom sensitivity
    val columbusCustomSensitivity: TapTapSetting<Float>

    //Which TensorFlow model to use - only works in normal mode
    val columbusTapModel: TapTapSetting<TapModel>

    //Which side the buttons are on in reachability
    val reachabilityLeftHanded: TapTapSetting<Boolean>

    //Feedback -> Vibrate
    val feedbackVibrate: TapTapSetting<Boolean>

    //Feedback -> Vibrate in DnD
    val feedbackVibrateDND: TapTapSetting<Boolean>

    //Feedback -> WakeDevice
    val feedbackWakeDevice: TapTapSetting<Boolean>

    //Whether to use the legacy wake method, which doesn't use a wakelock
    val advancedLegacyWake: TapTapSetting<Boolean>

    //Automatically restart the service periodically
    val advancedAutoRestart: TapTapSetting<Boolean>

    //Whether to use EXECUTION_PREFERENCE_LOW_POWER for the nnapidelegate (tensor only) - default on
    val advancedTensorLowPower: TapTapSetting<Boolean>

    //Whether the user has previously given sui permission - this is NOT used for the service, and is always verified
    val hasPreviouslyGrantedSui: TapTapSetting<Boolean>

    //Whether triple tap is enabled. This will be ignored and be disabled if there are no triple tap actions added
    val actionsTripleTapEnabled: TapTapSetting<Boolean>

    //Whether to show the help box on triple tap
    val actionsDoubleTapShowHelp: TapTapSetting<Boolean>

    //Whether to show the infobox on triple tap
    val actionsTripleTapShowHelp: TapTapSetting<Boolean>

    //Whether to show the help box on gates
    val gatesShowHelp: TapTapSetting<Boolean>

    //Whether to show the warning box on battery & optimisation. Will be overridden if the app is in low power mode.
    val batteryShowWarning: TapTapSetting<Boolean>

    abstract class TapTapSetting<T> {
        abstract suspend fun exists(): Boolean
        abstract fun existsSync(): Boolean
        abstract suspend fun set(value: T)
        abstract suspend fun get(): T
        abstract suspend fun getOrNull(): T?
        abstract suspend fun clear()
        abstract fun getSync(): T
        abstract fun asFlow(): Flow<T>
        abstract fun asFlowNullable(): Flow<T?>
    }

    /**
     *  Helper implementation of [TapTapSetting] that takes a regular StateFlow and calls a method
     *  ([onSet]) when [set] is called, allowing for external data to be handled by regular switch
     *  items. [clear] is not implemented, [exists] and [existsSync] will always return true.
     */
    class FakeTapTapSetting<T>(private val flow: StateFlow<T>, private val onSet: (value: T) -> Unit): TapTapSetting<T>() {

        override fun getSync(): T {
            return flow.value
        }

        override fun asFlow(): Flow<T> {
            return flow
        }

        override fun asFlowNullable(): Flow<T?> {
            throw RuntimeException("Not implemented!")
        }

        override suspend fun set(value: T) {
            onSet(value)
        }

        override suspend fun get(): T {
            return flow.value
        }

        override suspend fun getOrNull(): T? {
            return if(exists()){
                get()
            }else{
                null
            }
        }

        override suspend fun exists(): Boolean {
            return true
        }

        override fun existsSync(): Boolean {
            return true
        }

        override suspend fun clear() {
            throw RuntimeException("Not implemented!")
        }

    }

}

class TapTapSettingsImpl(context: Context) : TapTapSettings {

    companion object {
        const val KEY_SETTINGS_VERSION = "settings_version"
        const val DEFAULT_SETTINGS_VERSION = -1

        const val KEY_SETTINGS_UPGRADED = "settings_upgraded"
        const val DEFAULT_SETTINGS_UPGRADED = false

        const val KEY_HAS_SEEN_SETUP = "has_seen_setup"
        const val DEFAULT_HAS_SEEN_SETUP = false

        const val KEY_ENABLE_CRASH_REPORTING = "enable_crash_reporting"
        const val DEFAULT_ENABLE_CRASH_REPORTING = true

        const val KEY_MONET_COLOR = "monet_color"

        const val KEY_SERVICE_ENABLED = "service_enabled"
        const val DEFAULT_SERVICE_ENABLED = false //Gets enabled after skip or during setup

        const val KEY_INTERNET_ALLOWED = "internet_allowed"
        const val DEFAULT_INTERNET_ALLOWED = false

        const val KEY_BACKGROUND_UPDATE_CHECK = "background_update_check"
        const val DEFAULT_BACKGROUND_UPDATE_CHECK = false

        const val KEY_LOW_POWER_MODE = "low_power_mode"
        const val DEFAULT_LOW_POWER_MODE = false

        const val KEY_COLUMBUS_SENSITIVITY_LEVEL = "columbus_sensitivity_level"
        const val DEFAULT_COLUMBUS_SENSITIVITY_LEVEL = 6

        const val KEY_COLUMBUS_TAP_MODEL = "columbus_tap_model"
        val DEFAULT_COLUMBUS_TAP_MODEL = TapModel.BRAMBLE

        const val KEY_REACHABILITY_LEFT_HANDED = "reachability_left_handed"
        const val DEFAULT_REACHABILITY_LEFT_HANDED = false

        const val KEY_FEEDBACK_VIBRATE = "feedback_vibrate"
        const val DEFAULT_FEEDBACK_VIBRATE = true

        const val KEY_FEEDBACK_VIBRATE_DND = "feedback_vibrate_dnd"
        const val DEFAULT_FEEDBACK_VIBRATE_DND = false

        const val KEY_FEEDBACK_WAKE_DEVICE = "feedback_wake_device"
        const val DEFAULT_FEEDBACK_WAKE_DEVICE = false

        const val KEY_ADVANCED_AUTO_RESTART = "advanced_auto_restart"
        const val DEFAULT_ADVANCED_AUTO_RESTART = false

        const val KEY_ADVANCED_LEGACY_WAKE = "advanced_legacy_wake"
        const val DEFAULT_ADVANCED_LEGACY_WAKE = false

        const val KEY_ADVANCED_CUSTOM_SENSITIVITY_CHRE = "advanced_custom_sensitivity_chre"
        const val KEY_ADVANCED_CUSTOM_SENSITIVITY = "advanced_custom_sensitivity"

        const val KEY_ADVANCED_TENSOR_LOW_POWER = "advanced_tensor_low_power"
        const val DEFAULT_ADVANCED_TENSOR_LOW_POWER = true

        const val KEY_HAS_PREVIOUSLY_GRANTED_SUI = "has_previously_granted_sui"
        const val DEFAULT_HAS_PREVIOUSLY_GRANTED_SUI = false

        const val KEY_ACTIONS_TRIPLE_TAP_ENABLED = "actions_triple_tap_enabled"
        const val DEFAULT_ACTIONS_TRIPLE_TAP_ENABLED = false

        const val KEY_ACTIONS_TRIPLE_TAP_SHOW_HELP = "actions_triple_tap_show_help"
        const val DEFAULT_ACTIONS_TRIPLE_TAP_SHOW_HELP = true

        const val KEY_ACTIONS_DOUBLE_TAP_SHOW_HELP = "actions_double_tap_show_help"
        const val DEFAULT_ACTIONS_DOUBLE_TAP_SHOW_HELP = true

        const val KEY_GATES_SHOW_HELP = "gates_show_help"
        const val DEFAULT_GATES_SHOW_HELP = true

        const val KEY_BATTERY_SHOW_WARNING = "battery_show_warning"
        const val DEFAULT_BATTERY_SHOW_WARNING = true
    }

    override val settingsVersion: TapTapSettings.TapTapSetting<Int> = TapTapSettingImpl(
        KEY_SETTINGS_VERSION,
        DEFAULT_SETTINGS_VERSION,
        SHARED_INT
    )

    override val settingsUpgraded: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_SETTINGS_UPGRADED,
        DEFAULT_SETTINGS_UPGRADED,
        SHARED_BOOLEAN
    )

    override val hasSeenSetup: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_HAS_SEEN_SETUP,
        DEFAULT_HAS_SEEN_SETUP,
        SHARED_BOOLEAN
    )

    override val enableCrashReporting: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_ENABLE_CRASH_REPORTING,
        DEFAULT_ENABLE_CRASH_REPORTING,
        SHARED_BOOLEAN
    )

    override val monetColor: TapTapSettings.TapTapSetting<Int> = TapTapSettingImpl(
        KEY_MONET_COLOR,
        Integer.MAX_VALUE,
        SHARED_COLOR
    )

    override val serviceEnabled: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_SERVICE_ENABLED,
        DEFAULT_SERVICE_ENABLED,
        SHARED_BOOLEAN
    )
    
    override val internetAllowed: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_INTERNET_ALLOWED,
        DEFAULT_INTERNET_ALLOWED,
        SHARED_BOOLEAN
    )
    
    override val backgroundUpdateCheck: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_BACKGROUND_UPDATE_CHECK,
        DEFAULT_BACKGROUND_UPDATE_CHECK,
        SHARED_BOOLEAN
    )

    override val lowPowerMode: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_LOW_POWER_MODE,
        DEFAULT_LOW_POWER_MODE,
        SHARED_BOOLEAN
    )

    override val columbusSensitivityLevel: TapTapSettings.TapTapSetting<Int> = TapTapSettingImpl(
        KEY_COLUMBUS_SENSITIVITY_LEVEL,
        DEFAULT_COLUMBUS_SENSITIVITY_LEVEL,
        SHARED_INT
    )

    override val columbusCHRELowSensitivity: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_ADVANCED_CUSTOM_SENSITIVITY_CHRE,
        false,
        SHARED_BOOLEAN
    )

    override val columbusCustomSensitivity: TapTapSettings.TapTapSetting<Float> = TapTapSettingImpl(
        KEY_ADVANCED_CUSTOM_SENSITIVITY,
        -1f,
        SHARED_FLOAT
    )

    override val columbusTapModel: TapTapSettings.TapTapSetting<TapModel> = TapTapSettingImpl(
        KEY_COLUMBUS_TAP_MODEL,
        DEFAULT_COLUMBUS_TAP_MODEL
    ) { _, key, default -> sharedEnum(key, default) }

    override val reachabilityLeftHanded: TapTapSettings.TapTapSetting<Boolean> = TapTapSettingImpl(
        KEY_REACHABILITY_LEFT_HANDED,
        DEFAULT_REACHABILITY_LEFT_HANDED,
        SHARED_BOOLEAN
    )

    override val feedbackVibrate: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_FEEDBACK_VIBRATE, DEFAULT_FEEDBACK_VIBRATE, SHARED_BOOLEAN)

    override val feedbackVibrateDND: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_FEEDBACK_VIBRATE_DND, DEFAULT_FEEDBACK_VIBRATE_DND, SHARED_BOOLEAN)

    override val feedbackWakeDevice: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_FEEDBACK_WAKE_DEVICE, DEFAULT_FEEDBACK_WAKE_DEVICE, SHARED_BOOLEAN)

    override val advancedLegacyWake: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ADVANCED_LEGACY_WAKE, DEFAULT_ADVANCED_LEGACY_WAKE, SHARED_BOOLEAN)

    override val advancedAutoRestart: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ADVANCED_AUTO_RESTART, DEFAULT_ADVANCED_AUTO_RESTART, SHARED_BOOLEAN)

    override val advancedTensorLowPower: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ADVANCED_TENSOR_LOW_POWER, DEFAULT_ADVANCED_TENSOR_LOW_POWER, SHARED_BOOLEAN)

    override val hasPreviouslyGrantedSui: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_HAS_PREVIOUSLY_GRANTED_SUI, DEFAULT_HAS_PREVIOUSLY_GRANTED_SUI, SHARED_BOOLEAN)

    override val actionsTripleTapEnabled: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ACTIONS_TRIPLE_TAP_ENABLED, DEFAULT_ACTIONS_TRIPLE_TAP_ENABLED, SHARED_BOOLEAN)

    override val actionsTripleTapShowHelp: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ACTIONS_TRIPLE_TAP_SHOW_HELP, DEFAULT_ACTIONS_TRIPLE_TAP_SHOW_HELP, SHARED_BOOLEAN)

    override val actionsDoubleTapShowHelp: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_ACTIONS_DOUBLE_TAP_SHOW_HELP, DEFAULT_ACTIONS_DOUBLE_TAP_SHOW_HELP, SHARED_BOOLEAN)

    override val gatesShowHelp: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_GATES_SHOW_HELP, DEFAULT_GATES_SHOW_HELP, SHARED_BOOLEAN)

    override val batteryShowWarning: TapTapSettings.TapTapSetting<Boolean> =
        TapTapSettingImpl(KEY_BATTERY_SHOW_WARNING, DEFAULT_BATTERY_SHOW_WARNING, SHARED_BOOLEAN)

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)
    }

    private fun shared(key: String, default: Boolean) = ReadWriteProperty({
        sharedPreferences.getBoolean(key, default)
    }, {
        sharedPreferences.edit().putBoolean(key, it).commit()
    })

    private fun shared(key: String, default: String) = ReadWriteProperty({
        sharedPreferences.getString(key, default) ?: default
    }, {
        sharedPreferences.edit().putString(key, it).commit()
    })

    private fun shared(key: String, default: Int) = ReadWriteProperty({
        sharedPreferences.getInt(key, default)
    }, {
        sharedPreferences.edit().putInt(key, it).commit()
    })

    private fun shared(key: String, default: Float) = ReadWriteProperty({
        sharedPreferences.getFloat(key, default)
    }, {
        sharedPreferences.edit().putFloat(key, it).commit()
    })

    private fun sharedColor(key: String, unusedDefault: Int) = ReadWriteProperty({
        val rawColor = sharedPreferences.getString(key, "") ?: ""
        if(rawColor.isEmpty()) Integer.MAX_VALUE
        else Color.parseColor(rawColor)
    }, {
        sharedPreferences.edit().putString(key, it.toHexString()).commit()
    })

    private inline fun <reified T> sharedList(
        key: String,
        default: List<T>,
        crossinline transform: (List<T>) -> String,
        crossinline reverseTransform: (String) -> List<T>
    ) = ReadWriteProperty({
        reverseTransform(sharedPreferences.getString(key, null) ?: transform(default))
    }, {
        sharedPreferences.edit().putString(key, transform(it)).commit()
    })

    private inline fun <reified T : Enum<T>> sharedEnum(
        key: String,
        default: Enum<T>
    ): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {

            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return java.lang.Enum.valueOf(
                    T::class.java,
                    sharedPreferences.getString(key, default.name)
                )
            }

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                sharedPreferences.edit().putString(key, value.name).commit()
            }

        }
    }

    private inline fun <T> ReadWriteProperty(
        crossinline getValue: () -> T,
        crossinline setValue: (T) -> Unit
    ): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {

            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return getValue.invoke()
            }

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                setValue.invoke(value)
            }

        }
    }

    private fun stringListTypeConverter(list: List<String>): String {
        if (list.isEmpty()) return ""
        return list.joinToString(",")
    }

    private fun stringListTypeReverseConverter(pref: String): List<String> {
        if (pref.isEmpty()) return emptyList()
        if (!pref.contains(",")) return listOf(pref.trim())
        return pref.split(",")
    }

    object SettingsConverters {
        internal val SHARED_INT: (TapTapSettingsImpl, String, Int) -> ReadWriteProperty<Any?, Int> =
            TapTapSettingsImpl::shared
        internal val SHARED_STRING: (TapTapSettingsImpl, String, String) -> ReadWriteProperty<Any?, String> =
            TapTapSettingsImpl::shared
        internal val SHARED_BOOLEAN: (TapTapSettingsImpl, String, Boolean) -> ReadWriteProperty<Any?, Boolean> =
            TapTapSettingsImpl::shared
        internal val SHARED_FLOAT: (TapTapSettingsImpl, String, Float) -> ReadWriteProperty<Any?, Float> =
            TapTapSettingsImpl::shared
        internal val SHARED_COLOR: (TapTapSettingsImpl, String, Int) -> ReadWriteProperty<Any?, Int> =
            TapTapSettingsImpl::sharedColor
    }

    inner class TapTapSettingImpl<T>(
        private val key: String,
        private val default: T,
        shared: (TapTapSettingsImpl, String, T) -> ReadWriteProperty<Any?, T>
    ) : TapTapSettings.TapTapSetting<T>() {

        private var rawSetting by shared(this@TapTapSettingsImpl, key, default)

        override suspend fun exists(): Boolean {
            return withContext(Dispatchers.IO) {
                sharedPreferences.contains(key)
            }
        }

        /**
         *  Should only be used where there is no alternative
         */
        override fun existsSync(): Boolean {
            return runBlocking {
                exists()
            }
        }

        override suspend fun set(value: T) {
            withContext(Dispatchers.IO) {
                rawSetting = value
            }
        }

        override suspend fun get(): T {
            return withContext(Dispatchers.IO) {
                rawSetting ?: default
            }
        }

        override suspend fun getOrNull(): T? {
            return if(exists()){
                get()
            }else null
        }

        /**
         *  Should only be used where there is no alternative
         */
        override fun getSync(): T {
            return runBlocking {
                get()
            }
        }

        override suspend fun clear() {
            withContext(Dispatchers.IO) {
                sharedPreferences.edit().remove(key).commit()
            }
        }

        override fun asFlow() = callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                trySend(rawSetting ?: default)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(rawSetting ?: default)
            awaitClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }.flowOn(Dispatchers.IO)

        override fun asFlowNullable() = callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                trySend(rawSetting)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            if(existsSync()) trySend(rawSetting)
            awaitClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }.flowOn(Dispatchers.IO)

    }

}

suspend fun TapTapSettings.TapTapSetting<Boolean>.invert() {
    set(!get())
}