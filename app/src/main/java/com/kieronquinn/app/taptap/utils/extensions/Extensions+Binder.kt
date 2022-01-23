package com.kieronquinn.app.taptap.utils.extensions

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.system.Os

fun Binder.isRoot(): Boolean {
    return Binder.getCallingUid() == 0
}

/**
 *  Sets the Binder's UID to `2000` (`shell`), if it's currently running as `0` (`root`).
 *
 *  This is needed to access some system services as shell
 */
fun Binder.makeShellIfNeeded() {
    if(isRoot()){
        @Suppress("DEPRECATION") // root is able to call this fine
        Os.setuid(2000)
    }
}

fun Parcel.writeNullableString(value: String?){
    if(value != null){
        writeBooleanCompat(false)
        writeString(value)
    }else{
        writeBooleanCompat(true)
    }
}

fun Parcel.readNullableString(): String? {
    val isNull = readBooleanCompat()
    return if(!isNull){
        readString()
    }else null
}

fun Parcel.readStrongBinderOptional(): IBinder? {
    val isNull = readBooleanCompat()
    return if(isNull) null
    else readStrongBinder()
}

fun Parcel.writeStrongBinderOptional(binder: IBinder?){
    writeBooleanCompat(binder == null)
    if(binder != null){
        writeStrongBinder(binder)
    }
}

fun Parcel.writeBooleanCompat(value: Boolean){
    writeInt(if(value) 1 else 0)
}

fun Parcel.readBooleanCompat(): Boolean {
    return readInt() == 1
}

fun Parcel.writeNullableInt(value: Int?) {
    writeBooleanCompat(value != null)
    if(value != null){
        writeInt(value)
    }
}

fun Parcel.readNullableInt(): Int? {
    return if(readBooleanCompat()){
        readInt()
    }else null
}