package com.kieronquinn.app.taptap.utils.extensions

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import com.kieronquinn.app.taptap.utils.SettingsContentObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.reflect.InvocationTargetException

suspend inline fun <reified T: Any> Class<out Settings.NameValueTable>.getSettingAsFlow(settingsField: String, contentResolver: ContentResolver, updateOnStart: Boolean = false): Flow<T?> = channelFlow<T?> {
    val uri = getUriFor(settingsField)
    var contentObserver: ContentObserver?
    when(T::class){
        String::class -> {
            val update = {
                offer(getString(contentResolver, settingsField) as T?)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(
                Handler(Looper.myLooper()!!),
                uri
            ) { _, _ ->
                update.invoke()
            }.apply {
                if(updateOnStart) update.invoke()
                contentObserver = this
            })
        }
        Float::class -> {
            val update = {
                offer(getFloat(contentResolver, settingsField) as T?)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(
                Handler(Looper.myLooper()!!),
                uri
            ) { _, _ ->
                update.invoke()
            }.apply {
                if(updateOnStart) update.invoke()
                contentObserver = this
            })
        }
        Long::class -> {
            val update = {
                offer(getLong(contentResolver, settingsField) as T?)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(
                Handler(Looper.myLooper()!!),
                uri
            ) { _, _ ->
                update.invoke()
            }.apply {
                if(updateOnStart) update.invoke()
                contentObserver = this
            })
        }
        Int::class -> {
            val update = {
                offer(getInt(contentResolver, settingsField) as T?)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(
                Handler(Looper.myLooper()!!),
                uri
            ) { _, _ ->
                update.invoke()
            }.apply {
                if(updateOnStart) update.invoke()
                contentObserver = this
            })
        }
        Boolean::class -> {
            val update = {
                offer((getInt(contentResolver, settingsField) == 1) as T)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(
                Handler(Looper.myLooper()!!),
                uri
            ) { _, _ ->
                update.invoke()
            }.apply {
                if(updateOnStart) update.invoke()
                contentObserver = this
            })
        }
        else -> throw IllegalArgumentException("${T::class.simpleName} cannot be stored in the Settings database $simpleName")
    }
    awaitClose {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }
}

fun Class<out Settings.NameValueTable>.getUriFor(uri: String): Uri {
    return getMethod("getUriFor", String::class.java).invoke(null, uri) as Uri
}

fun Class<out Settings.NameValueTable>.getString(contentResolver: ContentResolver, field: String): String? = try {
    getMethod("getString", ContentResolver::class.java, String::class.java).invoke(null, contentResolver, field) as? String
} catch (e: InvocationTargetException){
    null
}

fun Class<out Settings.NameValueTable>.getFloat(contentResolver: ContentResolver, field: String, default: Float? = null): Float? = try {
    if(default != null){
        getMethod("getFloat", ContentResolver::class.java, String::class.java, Float::class.java).invoke(null, contentResolver, field, default) as? Float
    }else{
        getMethod("getFloat", ContentResolver::class.java, String::class.java).invoke(null, contentResolver, field) as? Float
    }
} catch (e: InvocationTargetException){
    null
}

fun Class<out Settings.NameValueTable>.getInt(contentResolver: ContentResolver, field: String, default: Int? = null): Int? = try {
    if(default != null){
        getMethod("getInt", ContentResolver::class.java, String::class.java, Integer.TYPE).invoke(null, contentResolver, field, default) as? Int
    }else{
        getMethod("getInt", ContentResolver::class.java, String::class.java).invoke(null, contentResolver, field) as? Int
    }
} catch (e: InvocationTargetException){
    null
}

fun Class<out Settings.NameValueTable>.getLong(contentResolver: ContentResolver, field: String, default: Long? = null): Long? = try {
    if(default != null){
        getMethod("getLong", ContentResolver::class.java, String::class.java, Long::class.java).invoke(null, contentResolver, field, default) as? Long
    }else{
        getMethod("getLong", ContentResolver::class.java, String::class.java).invoke(null, contentResolver, field) as? Long
    }
} catch (e: InvocationTargetException){
    null
}

fun settingsGlobalGetIntOrNull(contentResolver: ContentResolver, key: String): Int? {
    return try {
        Settings.Global.getInt(contentResolver, key)
    }catch (e: Settings.SettingNotFoundException){
        null
    }
}

/**
 * Based on [com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings]
 * @see [AccessibilityUtils](https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java.L55)
 */
fun isAccessibilityServiceEnabled(context: Context, accessibilityService: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, accessibilityService)
    val enabledServicesSetting: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString: String = colonSplitter.next()
        val enabledService: ComponentName? = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService.equals(expectedComponentName)) return true
    }
    return false
}
