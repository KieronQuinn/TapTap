package com.kieronquinn.app.taptap.utils.extensions

public fun <T> Array<out T>.indexOfOrNull(element: T): Int? {
    if(!contains(element)) return null
    return indexOf(element)
}