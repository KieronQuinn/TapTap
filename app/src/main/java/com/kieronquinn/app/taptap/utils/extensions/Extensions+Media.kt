package com.kieronquinn.app.taptap.utils.extensions

fun isAudioStreamActive(audioStream: Int): Boolean {
    runCatching {
        val clazz = Class.forName("android.media.AudioSystem")
        val isStreamActive = clazz.getDeclaredMethod("isStreamActive", Integer.TYPE, Integer.TYPE)
        return isStreamActive.invoke(null, audioStream, 0) as Boolean
    }
    return false
}