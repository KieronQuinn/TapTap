package com.kieronquinn.app.taptap.utils.extensions

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.kieronquinn.app.taptap.ui.activities.MainActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

fun <T> Context.getSettingAsFlow(uri: Uri, converter: (Context) -> T) = callbackFlow<T?> {
    val observer = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            trySend(converter(this@getSettingAsFlow))
        }
    }
    trySend(converter(this@getSettingAsFlow))
    contentResolver.registerContentObserver(uri, false, observer)
    awaitClose {
        contentResolver.unregisterContentObserver(observer)
    }
}

fun Context.secureStringConverter(name: String): (Context) -> String? {
    return { _: Context ->
        Settings_Secure_getStringSafely(contentResolver, name)
    }
}

fun Context.secureBooleanConverter(name: String): (Context) -> Boolean {
    return { _: Context ->
        Settings_Secure_getIntSafely(contentResolver, name, 0) == 1
    }
}

private const val COLUMBUS_SETTING = "columbus_enabled"
private const val COLUMBUS_SETTING_ACTION = "columbus_action"
private const val COLUMBUS_SETTING_LAUNCH_APP = "columbus_launch_app"
private const val COLUMBUS_SETTING_LAUNCH_APP_SHORTCUT = "columbus_launch_app_shortcut"

fun Context.getColumbusSettingAsFlow(): Flow<Boolean>
    = getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING), secureBooleanConverter(COLUMBUS_SETTING)).filterNotNull()

fun Context.getColumbusNativeSettingsAsFlow() = combine(
    getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING), secureBooleanConverter(COLUMBUS_SETTING)).filterNotNull(),
    getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING_ACTION), secureStringConverter(COLUMBUS_SETTING_ACTION)).filterNotNull(),
    getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING_LAUNCH_APP), secureStringConverter(COLUMBUS_SETTING_LAUNCH_APP)).filterNotNull(),
    getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING_LAUNCH_APP_SHORTCUT), secureStringConverter(COLUMBUS_SETTING_LAUNCH_APP_SHORTCUT)).filterNotNull()
) { _, _, _, _ ->
    isNativeColumbusEnabled()
}

fun Context.getColumbusSetupNotificationRequiredFlow() = getSettingAsFlow(
    Settings.Secure.getUriFor(COLUMBUS_SETTING),
    secureBooleanConverter(COLUMBUS_SETTING)
).filterNotNull().drop(1).mapNotNull {
    if(isColumbusEnabled() && !isNativeColumbusEnabled()) Unit else null
}

fun Context.isColumbusEnabled(): Boolean {
    return Settings_Secure_getIntSafely(contentResolver, COLUMBUS_SETTING, 0) == 1
}

private fun Context.isColumbusActionSetToTapTap(): Boolean {
    if(Settings_Secure_getStringSafely(contentResolver, COLUMBUS_SETTING_ACTION) != "launch")
        return false
    val mainActivity = ComponentName(this, MainActivity::class.java).flattenToString()
    if(Settings_Secure_getStringSafely(contentResolver, COLUMBUS_SETTING_LAUNCH_APP) != mainActivity)
        return false
    if(Settings_Secure_getStringSafely(contentResolver, COLUMBUS_SETTING_LAUNCH_APP_SHORTCUT) != mainActivity)
        return false
    return true
}

fun Context.isNativeColumbusEnabled(): Boolean {
    return canUseContextHubLogging && isColumbusEnabled() && isColumbusActionSetToTapTap()
}

fun Settings_Secure_getIntSafely(contentResolver: ContentResolver, setting: String, default: Int): Int {
    return try {
        Settings.Secure.getInt(contentResolver, setting, default)
    }catch (e: Settings.SettingNotFoundException){
        default
    }
}

fun Settings_Secure_getStringSafely(contentResolver: ContentResolver, setting: String): String? {
    return try {
        Settings.Secure.getString(contentResolver, setting)
    }catch (e: Settings.SettingNotFoundException){
        null
    }
}

fun Settings_Global_getIntSafely(contentResolver: ContentResolver, setting: String, default: Int): Int {
    return try {
        Settings.Global.getInt(contentResolver, setting, default)
    }catch (e: Settings.SettingNotFoundException){
        default
    }
}