package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.util.Log
import com.kieronquinn.app.taptap.models.WhenGateInternal
import net.dinglisch.android.tasker.TaskerIntent

class TaskerTask(context: Context, private val taskName: String, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

    override fun isAvailable() = TaskerIntent.testStatus(context) == TaskerIntent.Status.OK && super.isAvailable()

    override fun onTrigger() {
        super.onTrigger()
        try {
            if(TaskerIntent.testStatus(context) == TaskerIntent.Status.OK){
                val taskerIntent = TaskerIntent(taskName)
                context.sendBroadcast(taskerIntent)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


}