package com.kieronquinn.app.taptap.components.columbus.feedback.custom

import android.content.Context
import android.os.VibrationEffect
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.utils.extensions.Settings_Global_getIntSafely
import com.kieronquinn.app.taptap.utils.extensions.vibrate
import org.koin.core.component.inject

class HapticClickFeedback(serviceLifecycle: Lifecycle, private val context: Context) :
    TapTapFeedbackEffect(serviceLifecycle) {

    private val dndMode
        get() = Settings_Global_getIntSafely(
            context.contentResolver,
            "zen_mode",
            0
        )

    private val settings by inject<TapTapSettings>()

    private val overrideDnd
        get() = settings.feedbackVibrateDND.getSync()

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if (!detectionProperties.isHapticConsumed && (dndMode == 0 || overrideDnd)) {
            if (isTripleTap) {
                context.vibrate(VibrationEffect.EFFECT_HEAVY_CLICK, 300)
            } else {
                context.vibrate(VibrationEffect.EFFECT_CLICK, 200)
            }
        }
    }

}