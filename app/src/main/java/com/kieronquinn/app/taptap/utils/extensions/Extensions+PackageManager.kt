package com.kieronquinn.app.taptap.utils.extensions

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun PackageManager.getApplicationInfoOrNull(packageName: String?): ApplicationInfo? {
    return try {
        packageName?.let { getApplicationInfo(it, 0) }
    }catch (e: PackageManager.NameNotFoundException){
        null
    }
}