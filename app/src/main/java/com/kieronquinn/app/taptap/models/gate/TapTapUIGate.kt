package com.kieronquinn.app.taptap.models.gate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TapTapUIGate(
    var id: Int,
    val gate: TapTapGateDirectory,
    var enabled: Boolean,
    val index: Int,
    val extraData: String,
    val description: CharSequence
) : Parcelable