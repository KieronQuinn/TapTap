package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate

class AlarmTimerAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "AlarmTimerAction"

    companion object {
        private val alarmIntentActions = arrayOf(AlarmClock.ACTION_DISMISS_ALARM, AlarmClock.ACTION_DISMISS_TIMER)
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        context.startActivities(alarmIntentActions.map { Intent(it).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }}.toTypedArray())
    }

}