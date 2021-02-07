package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.models.WhenGateInternal

class AlarmTimerAction(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

    companion object {
        private val alarmIntentActions = arrayOf(AlarmClock.ACTION_DISMISS_ALARM, AlarmClock.ACTION_DISMISS_TIMER)
    }

    override fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?) {
        super.onTrigger(detectionProperties)
        context.startActivities(alarmIntentActions.map { Intent(it).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }}.toTypedArray())
    }

}