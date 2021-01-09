package com.kieronquinn.app.taptap.utils

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

class SettingsContentObserver(handler: Handler, private val uri : Uri, private val listener: (Boolean, Uri) -> Unit) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        listener.invoke(selfChange, uri)
    }

}