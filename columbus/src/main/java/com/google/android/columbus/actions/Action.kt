package com.google.android.columbus.actions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor

abstract class Action(
    private val context: Context,
    private val feedbackEffects: Set<FeedbackEffect>
) {

    interface Listener {
        fun onActionAvailabilityChanged(action: Action)
    }

    protected val listeners = LinkedHashSet<Listener>()
    private val handler = Handler(Looper.getMainLooper())
    private var isAvailable = true
    abstract val tag: String

    open fun onGestureDetected(flags: Int, detectionProperties: GestureSensor.DetectionProperties?){
        updateFeedbackEffects(flags, detectionProperties)
        if(flags == 1){
            Log.i(tag, "Triggering")
            onTrigger(detectionProperties)
        }
    }

    abstract fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?)

    fun registerListener(listener: Listener){
        listeners.add(listener)
    }

    fun setAvailable(available: Boolean){
        if(isAvailable != available){
            isAvailable = available
            listeners.forEach {
                handler.post {
                    it.onActionAvailabilityChanged(this)
                }
            }

            if(isAvailable){
                handler.post {
                    updateFeedbackEffects( 0, null)
                }
            }
        }
    }

    open fun isAvailable() = isAvailable

    override fun toString(): String {
        return this::class.java.simpleName
    }

    fun unregisterListener(listener: Listener){
        listeners.remove(listener)
    }

    fun updateFeedbackEffects(flags: Int, detectionProperties: GestureSensor.DetectionProperties? = null){
        feedbackEffects.forEach {
            it.onGestureDetected(flags, detectionProperties)
        }
    }

}