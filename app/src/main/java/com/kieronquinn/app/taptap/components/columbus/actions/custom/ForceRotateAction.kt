package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate

class ForceRotateAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "ForceRotateAction"

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showSettingsPermissionNotificationIfNeeded()) return
        Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        val newRotation = when(context.resources.configuration.orientation){
            Configuration.ORIENTATION_LANDSCAPE -> 0 //Portrait
            Configuration.ORIENTATION_PORTRAIT -> 1 //Landscape
            else -> return //Unsupported
        }
        Settings.System.putInt(context.contentResolver, Settings.System.USER_ROTATION, newRotation)
    }

}