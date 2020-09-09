package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.util.Log
import com.kieronquinn.app.taptap.models.WhenGateInternal
import net.dinglisch.android.tasker.TaskerIntent

class TaskerTask(context: Context, private val taskName: String, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override fun isAvailable() = TaskerIntent.testStatus(context) == TaskerIntent.Status.OK && super.isAvailable()

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