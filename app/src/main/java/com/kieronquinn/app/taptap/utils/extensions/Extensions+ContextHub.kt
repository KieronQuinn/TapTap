package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.File

val Context.deviceHasContextHub: Boolean
    get() = packageManager.hasSystemFeature("android.hardware.context_hub") && doesShellHaveContextHubPermission()

fun ContextHub_hasColumbusNanoApp(): Boolean {
    val preloadedNanoAppsFile = File("/system/vendor/etc/chre", "preloaded_nanoapps.json")
    if(!preloadedNanoAppsFile.exists()) return false
    return try {
        val root = JSONObject(preloadedNanoAppsFile.readText())
        val nanoApps = root.getJSONArray("nanoapps") ?: return false
        nanoApps.contains("columbus")
    }catch (e: Exception){
        Log.d("TapTapContextHub", "Failed to read preloaded_nanoapps file", e)
        false
    }
}

private const val PERMISSION_LOCATION_HARDWARE = "android.permission.LOCATION_HARDWARE"
private const val PERMISSION_ACCESS_CONTEXT_HUB = "android.permission.ACCESS_CONTEXT_HUB"
private const val PACKAGE_SHELL = "com.android.shell"

/**
 *  Checks if Shell (`com.android.shell`) has the ability to access the context hub.
 *  On Android < S, `LOCATION_HARDWARE` is sufficient, but on S and above, `ACCESS_CONTEXT_HUB`
 *  is required.
 */
private fun Context.doesShellHaveContextHubPermission(): Boolean {
    return when {
        Build.VERSION.SDK_INT >= 33 -> {
            doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_ACCESS_CONTEXT_HUB)
        }
        else -> {
            doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_ACCESS_CONTEXT_HUB) || doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_LOCATION_HARDWARE)
        }
    }
}