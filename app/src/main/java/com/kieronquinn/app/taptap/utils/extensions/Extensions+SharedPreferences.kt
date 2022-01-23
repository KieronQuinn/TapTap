package com.kieronquinn.app.taptap.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.io.File

@SuppressLint("PrivateApi")
fun SharedPreferences_openFile(file: File): SharedPreferences {
    val sharedPreferencesImpl = Class.forName("android.app.SharedPreferencesImpl")
    return sharedPreferencesImpl.getDeclaredConstructor(File::class.java, Integer.TYPE)
        .apply {
            isAccessible = true
        }
        .newInstance(file, Context.MODE_PRIVATE) as SharedPreferences
}