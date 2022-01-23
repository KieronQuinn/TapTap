package com.kieronquinn.app.taptap.components.columbus

import android.util.Log
import com.google.android.columbus.ColumbusService
import com.google.android.columbus.PowerManagerWrapper
import com.google.android.columbus.actions.Action
import com.google.android.columbus.gates.Gate
import com.google.android.columbus.sensors.GestureController
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.components.columbus.sensors.ServiceEventEmitter
import com.kieronquinn.app.taptap.utils.extensions.runOnClose
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

/**
 *  Extension of [ColumbusService] to handle triple tap actions
 *
 *  Re-implements [updateSensorListener] and [stopListening] to handle triple tap,
 *  and adds a new action list. Gates, effects and sensors are untouched.
 */
class TapTapColumbusService(
    private val actions: List<TapTapAction>,
    private val tripleTapActions: List<TapTapAction>,
    private val effects: Set<TapTapFeedbackEffect>,
    private val gates: Set<TapTapGate>,
    private val gestureController: GestureController,
    powerManager: PowerManagerWrapper,
    scope: Scope,
    private val serviceEventEmitter: ServiceEventEmitter
) : ColumbusService(
    actions, effects, gates, gestureController.gestureSensor, powerManager
) {

    init {
        scope.runOnClose {
            //Stops listening
            updateSensorListener()
        }
    }

    companion object {
        private const val TAG = "Columbus/Service"
    }

    private var lastActiveTripleAction: Action? = null

    inner class TripleTapCapableGestureListener : GestureController.GestureListener {
        override fun onGestureDetected(
            sensor: GestureSensor,
            flags: Int,
            detectionProperties: GestureSensor.DetectionProperties
        ) {
            if (flags != 0) {
                wakeLock.acquire(2000L)
            }

            if (flags == 3) {
                if(blockingGate() != null) return
                //Triple tap
                val action = updateActiveTripleAction()
                if (action != null) {
                    action.onGestureDetected(flags, detectionProperties)
                    effects.forEach {
                        it.onGestureDetected(flags, detectionProperties)
                    }
                }
            } else {
                if(blockingGate() != null) return
                val action = updateActiveAction()
                if (action != null) {
                    action.onGestureDetected(flags, detectionProperties)
                    effects.forEach {
                        it.onGestureDetected(flags, detectionProperties)
                    }
                }
            }
        }
    }

    init {
        gestureController.setGestureListener(TripleTapCapableGestureListener())
        updateSensorListener()
    }

    override fun updateSensorListener() {
        //Hack - prevents call from ColumbusService before TapTapColumbusService is initialized.
        if (tripleTapActions == null) return

        val activeAction = updateActiveAction()
        val activeTripleAction = updateActiveTripleAction()
        if (activeAction == null && activeTripleAction == null) {
            Log.i(TAG, "No available actions")
            if(!passiveWhenGatesSet()) {
                deactivateGates()
                stopListening()
                return
            }else{
                Log.i(TAG, "Passive when gates are set, sensor listener will not be stopped")
            }
        }

        activateGates()
        val blockingGate = blockingActiveGate()
        if (blockingGate != null) {
            Log.i(TAG, "Gated by $blockingGate")
            stopListening()
            return
        }

        Log.i(TAG, "Unblocked, current action $activeAction and triple action $activeTripleAction")
        startListening()
    }

    override fun stopListening() {
        if (gestureController.stopListening()) {
            effects.forEach {
                it.onGestureDetected(0, null)
            }

            updateActiveAction()?.onGestureDetected(0, null)
            updateActiveTripleAction()?.onGestureDetected(0, null)
        }else{
            //Possibly need to suppress the loading notification
            GlobalScope.launch {
                serviceEventEmitter.postServiceEvent(ServiceEventEmitter.ServiceEvent.Started)
            }
        }
    }

    fun updateActiveTripleAction(): Action? {
        val firstAvailableAction = firstAvailableTripleAction()
        val lastActiveAction = lastActiveTripleAction
        if (firstAvailableAction != null && lastActiveAction != firstAvailableAction) {
            Log.i(
                TAG,
                "Switching triple tap action from $lastActiveAction to $firstAvailableAction"
            )
        }

        lastActiveTripleAction = firstAvailableAction
        return firstAvailableAction
    }

    private fun firstAvailableTripleAction(): Action? {
        return tripleTapActions.firstOrNull { it.isAvailable() }
    }

    /**
     *  Some custom gates are "passive", ie. they are not capable of notifying the service
     *  when they change state. When that happens, they should be skipped and we will instead
     *  always stay listening and check their state in the gesture calls.
     */
    private fun blockingActiveGate(): Gate? {
        return gates.firstOrNull { it !is PassiveGate && it.isBlocking }
    }

    /**
     *  Returns whether any of the actions' set gates are passive, ie. the sensor listener should
     *  not be stopped.
     */
    private fun passiveWhenGatesSet(): Boolean {
        return actions.any { it.passiveWhenGatesSet() }
    }

}