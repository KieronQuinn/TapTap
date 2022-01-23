package com.kieronquinn.app.taptap.utils.extensions

fun SystemProperties_getInt(key: String, defaultValue: Int): Int {
    runCatching {
        val systemProperties = Class.forName("android.os.SystemProperties")
        systemProperties.getMethod("getInt", String::class.java, Int::class.java).invoke(null, key, defaultValue) as? Int ?: defaultValue
    }.onSuccess {
        return it
    }.onFailure {
        return defaultValue
    }
    return defaultValue
}

fun SystemProperties_getBoolean(key: String, defaultValue: Boolean): Boolean {
    runCatching {
        val systemProperties = Class.forName("android.os.SystemProperties")
        systemProperties.getMethod("getBoolean", String::class.java, Boolean::class.java).invoke(null, key, defaultValue) as? Boolean ?: defaultValue
    }.onSuccess {
        return it
    }.onFailure {
        return defaultValue
    }
    return defaultValue
}

fun SystemProperties_getString(key: String, defaultValue: String): String {
    runCatching {
        val systemProperties = Class.forName("android.os.SystemProperties")
        systemProperties.getMethod("get", String::class.java, String::class.java).invoke(null, key, defaultValue) as? String ?: defaultValue
    }.onSuccess {
        return it
    }.onFailure {
        return defaultValue
    }
    return defaultValue
}