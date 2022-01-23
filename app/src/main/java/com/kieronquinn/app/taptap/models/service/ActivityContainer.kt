package com.kieronquinn.app.taptap.models.service

import android.app.IApplicationThread
import android.os.Parcel
import android.os.Parcelable
import com.kieronquinn.app.taptap.utils.extensions.readNullableInt
import com.kieronquinn.app.taptap.utils.extensions.readStrongBinderOptional
import com.kieronquinn.app.taptap.utils.extensions.writeNullableInt
import com.kieronquinn.app.taptap.utils.extensions.writeStrongBinderOptional

data class ActivityContainer(val thread: IApplicationThread?, val enterResId: Int? = null, val exitResId: Int? = null): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readStrongBinderOptional()?.let { IApplicationThread.Stub.asInterface(it) },
        parcel.readNullableInt(),
        parcel.readNullableInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStrongBinderOptional(thread?.asBinder())
        parcel.writeNullableInt(enterResId)
        parcel.writeNullableInt(exitResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActivityContainer> {
        override fun createFromParcel(parcel: Parcel): ActivityContainer {
            return ActivityContainer(parcel)
        }

        override fun newArray(size: Int): Array<ActivityContainer?> {
            return arrayOfNulls(size)
        }
    }
}