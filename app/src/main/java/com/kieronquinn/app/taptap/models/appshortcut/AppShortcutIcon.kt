package com.kieronquinn.app.taptap.models.appshortcut

import android.graphics.drawable.Icon
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class AppShortcutIcon(val icon: Icon? = null, val descriptor: ParcelFileDescriptor? = null): Parcelable

data class AppShortcutCachedIcon(val icon: Icon?, val cacheIcon: File?)