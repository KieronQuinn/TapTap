package com.kieronquinn.app.taptap.models.appshortcut

import android.content.pm.ShortcutQueryWrapper
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShortcutQueryWrapper(
    val queryFlags: Int
): Parcelable {

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun toSystemShortcutQueryWrapper(): ShortcutQueryWrapper {
        return ShortcutQueryWrapper().apply {
            queryFlags = this@ShortcutQueryWrapper.queryFlags
        }
    }

}
