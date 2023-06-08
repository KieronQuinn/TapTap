package com.kieronquinn.app.taptap.components.columbus.feedback

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.utils.extensions.runOnDestroy
import org.koin.core.component.KoinComponent

/**
 *  [FeedbackEffect] effect that has [Lifecycle] capabilities, to allow cleaning up when the
 *  lifecycle is destroyed (service dies). All FeedbackEffects should extend from this.
 */
abstract class TapTapFeedbackEffect(private val serviceLifecycle: Lifecycle): FeedbackEffect, LifecycleOwner, KoinComponent {

    init {
        serviceLifecycle.runOnDestroy {
            onDestroy()
        }
    }

    open fun onDestroy() {
        //Override if you want to have custom handling for when the lifecycle is destroyed
    }

    override fun onGestureDetected(
        flags: Int,
        detectionProperties: GestureSensor.DetectionProperties?
    ) {
        if(detectionProperties == null) return
        lifecycleScope.launchWhenCreated {
            when(flags){
                //Progress
                2 -> onProgress(detectionProperties)
                //Double tap
                1 -> onTriggered(detectionProperties, false)
                //Triple tap
                3 -> onTriggered(detectionProperties, true)
            }
        }

    }

    abstract suspend fun onTriggered(detectionProperties: GestureSensor.DetectionProperties, isTripleTap: Boolean)

    /**
     *  onProgress is called after a single tap is detected, and is used in Columbus to trigger
     *  the assistant animation. It's not currently used in Tap, Tap but may be used eventually.
     */
    open suspend fun onProgress(detectionProperties: GestureSensor.DetectionProperties){
        //No-op by default
    }

    override val lifecycle
        get() = serviceLifecycle

}