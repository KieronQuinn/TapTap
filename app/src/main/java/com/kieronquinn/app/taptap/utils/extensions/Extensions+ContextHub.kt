package com.kieronquinn.app.taptap.utils.extensions

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import rikka.sui.Sui
import java.io.File

val Context.deviceHasContextHub: Boolean
    get() = packageManager.hasSystemFeature("android.hardware.context_hub")

val Context.canUseContextHub: Boolean
    get() = deviceHasContextHub && ContextHub_hasColumbusNanoApp() && doesShellHaveContextHubPermission()

val Context.canEnableContextHubLogging: Boolean
    get() = deviceHasContextHub && ContextHub_hasColumbusNanoApp()

val Context.canUseContextHubLogging: Boolean
    get() = canEnableContextHubLogging && doesHaveLogPermission()

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
 *  On Android < T, `LOCATION_HARDWARE` is sufficient, but on T and above, `ACCESS_CONTEXT_HUB`
 *  is required.
 *
 *  If the user is using Sui on T+, this is acceptable as root still has access.
 */
fun Context.doesShellHaveContextHubPermission(): Boolean {
    return when {
        Build_isAtLeastT() -> {
            Sui.isSui() || doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_ACCESS_CONTEXT_HUB)
        }
        else -> {
            doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_ACCESS_CONTEXT_HUB) || doesPackageHavePermission(PACKAGE_SHELL, PERMISSION_LOCATION_HARDWARE)
        }
    }
}

fun Context.doesHaveLogPermission(): Boolean {
    return doesHavePermissions(Manifest.permission.READ_LOGS)
}