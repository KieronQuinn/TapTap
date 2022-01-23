package com.kieronquinn.app.taptap.utils.extensions

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull

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

fun Context.getColumbusSettingAsFlow(): Flow<Boolean>
    = getSettingAsFlow(Settings.Secure.getUriFor(COLUMBUS_SETTING), secureBooleanConverter(COLUMBUS_SETTING)).filterNotNull()

fun Context.isColumbusEnabled(): Boolean {
    return try {
        Settings_Secure_getIntSafely(contentResolver, COLUMBUS_SETTING, 0) == 1
    }catch (e: Settings.SettingNotFoundException){
        false
    }
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