package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LowBatteryGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context) {

    private var batteryLevel = context.broadcastReceiverAsFlow(Intent.ACTION_BATTERY_CHANGED)
        .map {
            getBatteryLevel()
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, getBatteryLevel())

    override fun isBlocked(): Boolean {
        return batteryLevel.value <= 0.15f
    }

    init {
        lifecycleScope.launchWhenCreated {
            batteryLevel.collect {
                notifyListeners()
            }
        }
    }

    private fun getBatteryLevel(): Float {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return 1f
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if(level == -1 || scale == -1) return 1f
        return level / scale.toFloat()
    }

}