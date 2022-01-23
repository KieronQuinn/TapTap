package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository

class ClickQuickSettingAction(
    serviceLifecycle: Lifecycle,
    context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>,
    private val service: TapTapShizukuServiceRepository,
    private val component: String
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "ClickQuickSetting"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        val result = service.runWithService {
            it.clickQuickSetting(component)
        }
        if(result is TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed){
            showShizukuNotification(result.reason.contentRes)
        }
    }

}