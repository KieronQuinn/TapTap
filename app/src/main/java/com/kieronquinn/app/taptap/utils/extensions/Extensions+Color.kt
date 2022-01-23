package com.kieronquinn.app.taptap.utils.extensions

fun Int.toHexString(): String {
    return "#" + Integer.toHexString(this)
}