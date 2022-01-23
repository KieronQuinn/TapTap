package com.kieronquinn.app.taptap.utils.extensions

import android.content.ComponentName
import android.content.pm.PackageManager

fun PackageManager.getApplicationLabel(packageName: String?): CharSequence? {
    return try {
        if(packageName == null) return null
        getApplicationLabel(getApplicationInfo(packageName, 0))
    }catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

fun PackageManager.getServiceLabel(component: ComponentName?): CharSequence? {
    return try {
        if(component == null) return null
        getServiceInfo(component, 0).loadLabel(this)
    } catch (e: PackageManager.NameNotFoundException){
        null
    }
}