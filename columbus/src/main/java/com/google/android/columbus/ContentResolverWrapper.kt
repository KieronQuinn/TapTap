package com.google.android.columbus

import android.content.Context
import android.database.ContentObserver
import android.net.Uri

class ContentResolverWrapper(context: Context) {

    private val contentResolver = context.contentResolver

    fun registerContentObserver(uri: Uri, notifyForDescendants: Boolean, observer: ContentObserver, unused: Int) {
        contentResolver.registerContentObserver(uri, notifyForDescendants, observer)
    }

    fun unregisterContentObserver(observer: ContentObserver) {
        contentResolver.unregisterContentObserver(observer)
    }

}