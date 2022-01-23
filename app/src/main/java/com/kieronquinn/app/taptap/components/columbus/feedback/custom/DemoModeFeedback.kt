package com.kieronquinn.app.taptap.components.columbus.feedback.custom

import androidx.lifecycle.Lifecycle
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.repositories.demomode.DemoModeRepository
import org.koin.core.component.inject

class DemoModeFeedback(serviceLifecycle: Lifecycle) :
    TapTapFeedbackEffect(serviceLifecycle) {

    private val demoModeRepository by inject<DemoModeRepository>()

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        //Not required for this
    }

    override suspend fun onProgress(detectionProperties: GestureSensor.DetectionProperties) {
        demoModeRepository.onTapDetected()
    }

}