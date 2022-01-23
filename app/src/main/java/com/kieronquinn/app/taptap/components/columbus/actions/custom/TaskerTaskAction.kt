package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import net.dinglisch.android.tasker.TaskerIntent

class TaskerTaskAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val taskName: String,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "TaskerTaskAction"

    override fun isAvailable() = TaskerIntent.testStatus(context) == TaskerIntent.Status.OK && super.isAvailable()

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
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