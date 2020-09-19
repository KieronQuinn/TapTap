package com.kieronquinn.app.taptap.services

import android.content.Context
import android.util.Log
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.columbus.actions.DoNothingAction
import com.kieronquinn.app.taptap.utils.setAccessibleR

class TapColumbusService(private val context: Context, doubleTapActions: List<Action>, var tripleTapActions: MutableList<Action>, private val feedbackEffects: Set<FeedbackEffect>, gates: Set<Gate>, gestureSensor: GestureSensor, powerManagerWrapper: PowerManagerWrapper) : ColumbusService(doubleTapActions, feedbackEffects, gates, gestureSensor, powerManagerWrapper, null) {

    init {
        gestureListener = GestureListener(this)
        gestureSensor.setGestureListener(gestureListener)
    }

    private var lastActiveActionTriple: Action? = null

    class GestureListener(private val columbusService: TapColumbusService): ColumbusService.GestureListener() {
        init {
            ColumbusService.GestureListener::class.java.getDeclaredField("this\$0").setAccessibleR(true).set(this, columbusService)
        }

        override fun onGestureProgress(gestureSensor: GestureSensor, actionType: Int, detectionProperties: DetectionProperties?) {
            //Only handle the triple tap gestures ourselves
            if(detectionProperties?.actionId == 3L){
                Log.d("TapColumbusService", "onTripleTapGesture")
                //Trigger the vibration too
                onTripleTapFeedback(detectionProperties)
                //And then the actual gesture
                onTripleTapGesture(detectionProperties)
            }else {
                super.onGestureProgress(gestureSensor, actionType, detectionProperties)
            }
        }

        private fun onTripleTapFeedback(detectionProperties: DetectionProperties) {
            columbusService.feedbackEffects.forEach {
                it.onProgress(3, detectionProperties)
            }
        }

        private fun onTripleTapGesture(detectionProperties: DetectionProperties){
            if (!isGated(columbusService.gates)) {
                columbusService.wakeLock.acquire(2000.toLong())
                val var5: Action = columbusService.updateActiveActionTriple()
                if (var5 != null) {
                    val var6 = StringBuilder()
                    var6.append("Triggering ")
                    var6.append(var5)
                    Log.i("Columbus/ColumbusService", var6.toString())
                    var5.onProgress(3, detectionProperties)
                }
            }
        }
    }

    private fun updateActiveActionTriple(): Action {
        val var1 = firstAvailableActionTriple()
        val var2 = lastActiveActionTriple
        if (var2 != null && var1 !== var2) {
            val var3 = java.lang.StringBuilder()
            var3.append("Switching action from ")
            var3.append(var2)
            var3.append(" to ")
            var3.append(var1)
            Log.i("Columbus/ColumbusService", var3.toString())
            var2.onProgress(0, null as DetectionProperties?)
        }
        lastActiveActionTriple = var1
        return var1
    }

    private fun firstAvailableActionTriple(): Action {
        val var1: Iterator<*> = tripleTapActions.iterator()
        var var2: Any?
        do {
            if (!var1.hasNext()) {
                var2 = null
                break
            }
            var2 = var1.next()
        } while (!(var2 as Action?)!!.isAvailable)
        return var2 as Action
    }

    fun setActions(actions: List<Action>){
        if(actions.isEmpty()){
            this.actions.apply {
                clear()
                add(DoNothingAction(context))
            }
        }else{
            this.actions.apply {
                clear()
                addAll(actions)
            }
        }
    }

}