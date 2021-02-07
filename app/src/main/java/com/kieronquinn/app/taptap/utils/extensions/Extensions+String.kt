package com.kieronquinn.app.taptap.utils.extensions

import android.text.TextUtils
import java.util.*

fun String.toCamelCase(): String {
    if (TextUtils.isEmpty(this)) {
        return this
    }
    return Character.toUpperCase(this[0]) + this.substring(1).toLowerCase(Locale.getDefault())
}