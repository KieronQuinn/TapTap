package com.kieronquinn.app.taptap.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhenGateInternal(val gate: TapGate, val isInverted: Boolean = true, var data: String? = null) : Parcelable