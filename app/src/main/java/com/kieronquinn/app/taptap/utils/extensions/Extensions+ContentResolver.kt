package com.kieronquinn.app.taptap.utils.extensions

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun ContentResolver.observerAsFlow(uri: Uri, notifyForDecedents: Boolean, handler: Handler = Handler(Looper.getMainLooper()), ignoreSelfChanges: Boolean = false) = callbackFlow<Uri?>{
    val observer = object: ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            if(ignoreSelfChanges && selfChange) return
            trySend(uri)
        }
    }
    registerContentObserver(uri, notifyForDecedents, observer)
    awaitClose {
        unregisterContentObserver(observer)
    }
}