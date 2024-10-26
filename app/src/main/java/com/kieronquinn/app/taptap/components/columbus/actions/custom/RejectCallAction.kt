package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.doesHavePermissions
import com.kieronquinn.app.taptap.utils.extensions.onCallStateChanged
import com.kieronquinn.app.taptap.utils.extensions.whenResumed

class RejectCallAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "RejectCallAction"

    private val telecomManager by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    private val telephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val phoneStateListener by lazy {
        context.onCallStateChanged()
    }

    init {
        lifecycle.whenResumed {
            phoneStateListener.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        if (!context.doesHavePermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS
            )
        ) return false
        if (telephonyManager.callState != TelephonyManager.CALL_STATE_RINGING && telephonyManager.callState != TelephonyManager.CALL_STATE_OFFHOOK) return false
        return super.isAvailable()
    }

    @SuppressLint("MissingPermission")
    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telecomManager.endCall()
        }
    }

}