package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.Intent
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.ActivityConfigTaskerEvent
import com.kieronquinn.app.taptap.activities.TaskerEventUpdate
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.isAppLaunchable
import com.kieronquinn.app.taptap.utils.isPackageAssistant

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