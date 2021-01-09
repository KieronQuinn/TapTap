package com.kieronquinn.app.taptap.utils

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty0

suspend inline fun <reified T: Any> Class<out Settings.NameValueTable>.getSettingAsFlow(settingsField: String, contentResolver: ContentResolver, updateOnStart: Boolean = false): Flow<T?> = channelFlow<T?> {
    val uri = getUriFor(settingsField)
    var contentObserver: ContentObserver?
    when(T::class){
        String::class -> {
            val update = {
                offer(getString(contentResolver, settingsField) as T?)
            }
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(Handler(Looper.myLooper()!!), uri) { _, _ ->
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
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(Handler(Looper.myLooper()!!), uri) { _, _ ->
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
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(Handler(Looper.myLooper()!!), uri) { _, _ ->
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
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(Handler(Looper.myLooper()!!), uri) { _, _ ->
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
            contentResolver.registerContentObserver(uri, false, SettingsContentObserver(Handler(Looper.myLooper()!!), uri) { _, _ ->
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
