package com.kieronquinn.app.taptap.utils.extensions

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat.getSystemService


/**
 * Check if the service is Running
 * @param serviceClass the class of the Service
 *
 * @return true if the service is running otherwise false
 */
@Suppress("DEPRECATION") // There's no replacement for this, and it still returns our own services which is what we need
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun String.doesContainComponentName(componentName: ComponentName): Boolean {
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(this)
    while (colonSplitter.hasNext()) {
        val componentNameString: String = colonSplitter.next()
        val enabledService: ComponentName? = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService == componentName) return true
    }
    return false
}