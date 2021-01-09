package com.kieronquinn.app.taptap.utils

import android.app.ActivityManager

val activityManagerNative: Any
    get() {
        return try {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        } catch (e: NoSuchMethodException) {
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
    }