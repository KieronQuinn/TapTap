package com.kieronquinn.app.taptap.models.service

import android.os.Parcel
import android.os.Parcelable

enum class SnapchatQuickTapState: Parcelable {

    ENABLED, DISABLED, NO_ROOT, ERROR;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SnapchatQuickTapState> {
        override fun createFromParcel(parcel: Parcel): SnapchatQuickTapState {
            return valueOf(parcel.readString()!!)
        }

        override fun newArray(size: Int): Array<SnapchatQuickTapState?> {
            return arrayOfNulls(size)
        }
    }

}