package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.taptap.ui.activities.ActivityConfigTaskerEvent
import com.kieronquinn.app.taptap.ui.activities.ActivityConfigTaskerEventTriple
import com.kieronquinn.app.taptap.ui.activities.TaskerEventUpdate
import com.kieronquinn.app.taptap.ui.activities.TaskerEventUpdateTriple
import com.kieronquinn.app.taptap.models.WhenGateInternal

class TaskerEvent(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

    override fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?) {
        super.onTrigger()
        val isTripleTap = detectionProperties?.actionId == 3L
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