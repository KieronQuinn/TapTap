package com.kieronquinn.app.taptap.models.columbus

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppShortcutData(
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("shortcut_id")
    val shortcutId: String,
    @SerializedName("label")
    val label: String
) : Parcelable