package com.kieronquinn.app.taptap.utils.extensions

import org.json.JSONArray

fun JSONArray.contains(value: String): Boolean {
    for(i in 0 until length()){
        if(get(i) == value) return true
    }
    return false
}