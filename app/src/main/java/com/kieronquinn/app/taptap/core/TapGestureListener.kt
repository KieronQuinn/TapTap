package com.kieronquinn.app.taptap.core

import android.util.Log
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.core.columbus.actions.DoNothingAction
import com.kieronquinn.app.taptap.utils.extensions.setAccessibleR

class TapGestureListener(private val columbusService: TapColumbusService): ColumbusService.GestureListener() {

    init {
        ColumbusService.GestureListener::class.java.getDeclaredField("this\$0").setAccessibleR(true).set(this, columbusService)
    }

    override fun onGestureProgress(gestureSensor: GestureSensor, actionType: Int, detectionProperties: GestureSensor.DetectionProperties?) {
        //Only handle the triple tap gestures ourselves
        if(detectionProperties?.actionId == 3L){
            Log.d("TapColumbusService", "onTripleTapGesture")
            val gated = ColumbusService.isGated(columbusService.gates)
            if(!gated) {
                //Trigger the vibration too
                onTripleTapFeedback(detectionProperties)
            }
            //And then the actual gesture
            onTripleTapGesture(detectionProperties, gated)
        }else {
            super.onGestureProgress(gestureSensor, actionType, detectionProperties)
        }
    }

    private fun onTripleTapFeedback(detectionProperties: GestureSensor.DetectionProperties) {
        columbusService.effects.forEach {
            it.onProgress(3, detectionProperties)
        }
    }

    private fun onTripleTapGesture(detectionProperties: GestureSensor.DetectionProperties, gated: Boolean){
        Log.d("Columbus/ColumbusService", "onTripleTapGesture")
        Log.d("Columbus/ColumbusService", "onTripleTapGesture gated $")
        if (!gated) {
            Log.d("Columbus/ColumbusService", "onTripleTapGesture not gated")
            columbusService.wakeLock.acquire(2000.toLong())
            val var5: Action? = columbusService.updateActiveActionTriple()
            Log.d("Columbus/ColumbusService", "got action ${var5?.javaClass?.simpleName}")
            if (var5 != null) {
                val var6 = StringBuilder()
                var6.append("Triggering ")
                var6.append(var5)
                Log.i("Columbus/ColumbusService", var6.toString())
                var5.onProgress(3, detectionProperties)
            }
        }
    }

    override fun shouldIgnoreAction(action: Action?): Boolean {
        return action == null || action is DoNothingAction
    }

}