package com.kieronquinn.app.taptap.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GateInternal(val gate: TapGate, var isActivated: Boolean, var data: String? = null) : Parcelable