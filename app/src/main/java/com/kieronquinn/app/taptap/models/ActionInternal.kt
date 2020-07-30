package com.kieronquinn.app.taptap.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActionInternal(val action: TapAction, val whenList : ArrayList<TapGate> = ArrayList(), var data: String? = null) : Parcelable