package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.google.android.systemui.columbus.gates.Gate

class TelephonyActivity(context: Context) : Gate(context) {
    private var isCallBlocked = false
    private val phoneStateListener: PhoneStateListener
    private val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private fun isCallBlocked(var1: Int?): Boolean {
        return var1 != null && var1 == 2
    }

    override fun isBlocked(): Boolean {
        return isCallBlocked
    }

    override fun onActivate() {
        var var1 = telephonyManager
        val var2: Int
        var2 = var1.callState
        isCallBlocked = isCallBlocked(var2)
        var1 = telephonyManager
        var1.listen(phoneStateListener, 32)
    }

    override fun onDeactivate() {
        val var1 = telephonyManager
        try {
            var1.listen(phoneStateListener, 0)
        } catch (e: NullPointerException) {
            //Nothing we can do here
        }
    }

    init {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(var1: Int, var2: String) {
                val var3 = isCallBlocked(var1)
                if (var3 != isCallBlocked) {
                    isCallBlocked = var3
                    notifyListener()
                }
            }
        }
    }
}