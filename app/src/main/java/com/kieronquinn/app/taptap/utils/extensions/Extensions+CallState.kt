package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context.onCallStateChanged(): Flow<Unit> {
    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        telephonyManager.onCallStateChangedS(this)
    } else {
        telephonyManager.onCallStateChanged()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun TelephonyManager.onCallStateChangedS(context: Context) = callbackFlow {
    val listener = object: TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            trySend(Unit)
        }
    }
    registerTelephonyCallback(ContextCompat.getMainExecutor(context), listener)
    awaitClose {
        unregisterTelephonyCallback(listener)
    }
}

private fun TelephonyManager.onCallStateChanged() = callbackFlow {
    val listener = object: PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            trySend(Unit)
        }
    }
    listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    awaitClose {
        listen(listener, PhoneStateListener.LISTEN_NONE)
    }
}