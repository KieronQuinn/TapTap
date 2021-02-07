package com.kieronquinn.app.taptap.utils.extensions

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun String.gzip(): ByteArray {
    return ByteArrayOutputStream().apply {
        GZIPOutputStream(this).bufferedWriter().use { it.write(this@gzip) }
    }.toByteArray()
}

fun ByteArray.ungzip(): String {
    return GZIPInputStream(this.inputStream()).bufferedReader().use { it.readText() }
}