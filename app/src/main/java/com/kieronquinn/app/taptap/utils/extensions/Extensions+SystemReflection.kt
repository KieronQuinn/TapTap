package com.kieronquinn.app.taptap.utils.extensions

import android.app.ActivityManager
import java.lang.reflect.Field
import java.lang.reflect.Method

val activityManagerNative: Any
    get() {
        return try {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        } catch (e: NoSuchMethodException) {
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
    }

/*
    setAccessible, just returning the Field/Method to so it can be inlined
 */
fun Field.setAccessibleR(accessible: Boolean): Field {
    this.isAccessible = accessible
    return this
}

fun Method.setAccessibleR(accessible: Boolean): Method {
    this.isAccessible = accessible
    return this
}