package com.kieronquinn.app.taptap.components.columbus.gates

import androidx.lifecycle.Lifecycle
import com.google.android.columbus.gates.Gate
import com.kieronquinn.app.taptap.utils.extensions.runOnDestroy

/**
 *  Wrapper for [TapTapGate] which allows easy inversion. Used for sub-gates, ie. gates
 *  applied to specific actions.
 */
data class TapTapWhenGate(private val gate: TapTapGate, private val isInverted: Boolean) :
    TapTapGate(gate.lifecycle, gate.context), Gate.Listener {

    init {
        gate.registerListener(this)
        gate.maybeActivate()
        lifecycle.runOnDestroy {
            gate.unregisterListener(this)
            gate.maybeDeactivate()
        }
    }

    override fun isBlocked(): Boolean {
        return if (isInverted) {
            !gate.isBlocked()
        } else {
            gate.isBlocked()
        }
    }

    override fun onActivate() {
        gate.onActivate()
    }

    override fun onDeactivate() {
        gate.onDeactivate()
    }

    override fun onDestroy() {
        gate.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return gate.lifecycle
    }

    override fun onGateChanged(gate: Gate) {
        notifyListeners()
    }

    fun isPassive() = gate is PassiveGate

}
