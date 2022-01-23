package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.ui.activities.ActivityConfigTaskerEvent
import com.kieronquinn.app.taptap.ui.activities.ActivityConfigTaskerEventTriple
import com.kieronquinn.app.taptap.ui.activities.TaskerEventUpdate
import com.kieronquinn.app.taptap.ui.activities.TaskerEventUpdateTriple

class TaskerEventAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "TaskerEventAction"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        try {
            if(isTripleTap) {
                ActivityConfigTaskerEventTriple::class.java.requestQuery(context,
                    TaskerEventUpdateTriple()
                )
            }else{
                ActivityConfigTaskerEvent::class.java.requestQuery(context,
                    TaskerEventUpdate()
                )
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}