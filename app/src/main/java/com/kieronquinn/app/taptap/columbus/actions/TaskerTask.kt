package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.ActivityConfigTaskerEvent
import com.kieronquinn.app.taptap.activities.TaskerEventUpdate
import com.kieronquinn.app.taptap.utils.isAppLaunchable
import com.kieronquinn.app.taptap.utils.isPackageAssistant
import net.dinglisch.android.tasker.TaskerIntent

class TaskerTask(context: Context, private val taskName: String) : ActionBase(context) {

    override fun isAvailable() = TaskerIntent.testStatus(context) == TaskerIntent.Status.OK

    override fun onTrigger() {
        super.onTrigger()
        try {
            Log.d("TaskerTask", "Checking tasker ${TaskerIntent.testStatus(context)}")
            if(TaskerIntent.testStatus(context) == TaskerIntent.Status.OK){
                Log.d("TaskerTask", "Sending tasker task for $taskName")
                val taskerIntent = TaskerIntent(taskName)
                context.sendBroadcast(taskerIntent)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


}