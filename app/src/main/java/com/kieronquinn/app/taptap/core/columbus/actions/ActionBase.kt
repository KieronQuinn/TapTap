package com.kieronquinn.app.taptap.core.columbus.actions

import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.models.getGate

abstract class ActionBase(context: Context, private val whenGates: List<WhenGateInternal>) : Action(context, emptyList()) {

    open val requiresUnlock = false

    private val application by lazy {
        context.applicationContext as? TapTapApplication
    }

    private val keyguardManager by lazy {
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    private val loadedGates by lazy {
        whenGates.mapNotNull { getGate(context, it.gate, it.data)?.apply { activate() } }
    }

    override fun onProgress(var1: Int, var2: GestureSensor.DetectionProperties?) {
        if(var1 != 3) return

        if(requiresUnlock && keyguardManager.isKeyguardLocked){
            application?.requireUnlock = true
        }

        onTrigger()
        onTrigger(var2)
        triggerListener?.invoke()
    }

    open fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?){
        //Optional override for if the event cares if it's double or triple tap
    }

    var triggerListener: (() -> Unit)? = null

    override fun isAvailable(): Boolean {
        if(whenGates.isEmpty()) return true
        return loadedGates.any { it.isBlocking }
    }
}