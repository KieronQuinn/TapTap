package com.kieronquinn.app.taptap.models.gate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TapTapUIWhenGate(val id: Int, val gate: TapTapUIGate, val inverted: Boolean): Parcelable
