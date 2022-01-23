package com.kieronquinn.app.taptap.models.action

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TapTapUIAction(
    val tapAction: TapTapActionDirectory,
    var id: Int,
    val index: Int,
    val extraData: String,
    val description: CharSequence,
    var whenGatesSize: Int
): Parcelable