package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import com.kieronquinn.app.taptap.utils.extensions.isPowerConnected
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChargingStateGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context) {

    private val batteryState = context.broadcastReceiverAsFlow(Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED)
        .map {
            it.action == Intent.ACTION_POWER_CONNECTED
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, context.isPowerConnected())

    init {
        lifecycleScope.launchWhenCreated {
            batteryState.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        return batteryState.value
    }

}