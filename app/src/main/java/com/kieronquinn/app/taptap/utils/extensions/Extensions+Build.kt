package com.kieronquinn.app.taptap.utils.extensions

import android.annotation.SuppressLint

val Build_EMULATOR
    get() = getSystemProperty("ro.kernel.qemu") == "1"

val isMiui: Boolean
    get() = getSystemProperty("ro.miui.ui.version.code")
        .isNotEmpty()

@SuppressLint("PrivateApi")
private fun getSystemProperty(key: String): String {
    val systemProperties = Class.forName("android.os.SystemProperties")
    return systemProperties.getMethod("get", String::class.java).invoke(null, key) as String
}