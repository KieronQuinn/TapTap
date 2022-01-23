package com.google.android.columbus

import android.util.Log
import com.google.android.columbus.actions.Action
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.gates.Gate
import com.google.android.columbus.sensors.GestureSensor

open class ColumbusService(
    private val actions: List<Action>,
    private val effects: Set<FeedbackEffect>,
    private val gates: Set<Gate>,
    private val gestureSensor: GestureSensor,
    private val powerManager: PowerManagerWrapper
) {

    companion object {

        private const val TAG = "Columbus/ColumbusService"

        fun isGated(gates: Set<Gate>): Boolean {
            gates.forEach {
                if(it.active && it.isBlocking){
                    Log.d(TAG, "Gated by ${it.javaClass.simpleName}; ignoring gesture")
                    return true
                }

                if(!it.isBlocking){
                    return@forEach
                }

                Log.d(TAG, "Gate ${it.javaClass.simpleName}, is blocking, but not active, ignoring")
            }
            Log.d(TAG, "Not gated, checked ${gates.size} gates")
            return false
        }
    }

    open inner class GestureListener: GestureSensor.Listener {
        override fun onGestureDetected(sensor: GestureSensor, flags: Int, detectionProperties: GestureSensor.DetectionProperties){
            if(flags != 0){
                wakeLock.acquire(2000L)
            }

            val action = updateActiveAction()
            if(action != null){
                if(blockingGate() != null) return
                action.onGestureDetected(flags, detectionProperties)
                effects.forEach {
                    it.onGestureDetected(flags, detectionProperties)
                }
            }
        }
    }

    private val actionListener = object: Action.Listener {
        override fun onActionAvailabilityChanged(action: Action) {
            updateSensorListener()
        }
    }

    private val gateListener = object: Gate.Listener {
        override fun onGateChanged(gate: Gate) {
            updateSensorListener()
        }
    }

    private val gestureListener = GestureListener()
    private var lastActiveAction: Action? = null
    private var lastProgressGesture: Long = 0L
    private var lastStage: Int = 0
    protected val wakeLock = powerManager.newWakeLock(1, TAG)

    init {
        actions.forEach {
            it.registerListener(actionListener)
        }
        gates.forEach {
            it.registerListener(gateListener)
        }
        updateSensorListener()
    }

    protected fun activateGates() {
        gates.forEach {
            it.maybeActivate()
        }
    }

    protected fun blockingGate(): Gate? {
        return gates.firstOrNull { it.isBlocking }
    }

    protected fun deactivateGates() {
        gates.forEach {
            it.maybeDeactivate()
        }
    }

    private fun firstAvailableAction(): Action? {
        return actions.firstOrNull { it.isAvailable() }
    }

    protected fun startListening() {
        if(!gestureSensor.isListening()){
            gestureSensor.startListening(true)
        }
    }

    open fun stopListening() {
        if(gestureSensor.isListening()){
            gestureSensor.stopListening()
            effects.forEach {
                it.onGestureDetected(0, null)
            }
            updateActiveAction()
        }
    }

    protected fun updateActiveAction(): Action? {
        val firstAvailableAction = firstAvailableAction()
        val lastAction = lastActiveAction
        if(lastAction != firstAvailableAction){
            Log.i(TAG, "Switching action from $lastAction to $firstAvailableAction")
        }
        lastActiveAction = firstAvailableAction
        return firstAvailableAction
    }

    open fun updateSensorListener() {
        val activeAction = updateActiveAction()
        if(activeAction == null) {
            Log.i(TAG, "No available actions")
            deactivateGates()
            stopListening()
            return
        }

        activateGates()
        val blockingGate = blockingGate()
        if(blockingGate != null){
            Log.i(TAG, "Gated by $blockingGate")
            stopListening()
            return
        }

        Log.i(TAG, "Unblocked; current action: $activeAction")
        startListening()
    }

}