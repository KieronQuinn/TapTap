package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.taptap.activities.ActivityConfigTaskerEvent
import com.kieronquinn.app.taptap.activities.TaskerEventUpdate
import com.kieronquinn.app.taptap.models.WhenGateInternal

class TaskerEvent(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override fun onTrigger() {
        super.onTrigger()
        try {
            ActivityConfigTaskerEvent::class.java.requestQuery(context, TaskerEventUpdate())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


}