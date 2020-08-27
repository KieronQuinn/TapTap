package com.kieronquinn.app.taptap.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
data class ActionInternal(val action: TapAction, val whenList : ArrayList<WhenGateInternal> = ArrayList(), var data: String? = null) : Parcelable {
    fun isBlocking(): Boolean {
        return whenList.isEmpty() && action.canBlock
    }
}