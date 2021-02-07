package com.kieronquinn.app.taptap.core.columbus.actions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.doesHavePermissions

class AcceptCall(context: Context, whenGates: List<WhenGateInternal>): ActionBase(context, whenGates) {

    private val telecomManager by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    private val telephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val phoneStateListener = object: PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            notifyListener()
        }
    }

    override fun isAvailable(): Boolean {
        if(!context.doesHavePermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS)) return false
        if(telephonyManager.callState != TelephonyManager.CALL_STATE_RINGING) return false
        return super.isAvailable()
    }

    @SuppressLint("MissingPermission")
    override fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?) {
        super.onTrigger(detectionProperties)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && telephonyManager.callState == TelephonyManager.CALL_STATE_RINGING) {
            telecomManager.acceptRingingCall()
        }
    }

    override fun setListener(p0: Listener?) {
        super.setListener(p0)
        if(listener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }else{
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

}