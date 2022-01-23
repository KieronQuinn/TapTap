package com.kieronquinn.app.taptap.utils.extensions

import android.app.PendingIntent
import android.content.IIntentSender
import android.content.Intent

val PendingIntent.mTarget: IIntentSender
    get() {
        return this::class.java.getDeclaredField("mTarget").apply {
            isAccessible = true
        }.get(this) as IIntentSender
    }