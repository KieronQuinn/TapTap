package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BatterySaverGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var batterySaverEnabled = context.broadcastReceiverAsFlow(ACTION_POWER_SAVE_MODE_CHANGED)
        .map {
            powerManager.isPowerSaveMode
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, powerManager.isPowerSaveMode)

    override fun isBlocked(): Boolean {
        return batterySaverEnabled.value
    }

    init {
        lifecycleScope.launchWhenCreated {
            batterySaverEnabled.collect {
                notifyListeners()
            }
        }
    }

}