package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.getGate

abstract class ActionBase(context: Context, private val whenGates: List<WhenGateInternal>) : Action(context, emptyList()) {

    private val loadedGates by lazy {
        whenGates.mapNotNull { getGate(context, it.gate, it.data)?.apply { activate() } }
    }

    override fun onProgress(var1: Int, var2: GestureSensor.DetectionProperties?) {
        Log.d("ActionBase", "onProgress $var1")
        if(var1 != 3) return

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