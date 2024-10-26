package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PowerStateGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    //Special case: This gate is "passive" as CHRE has issues starting listening when locked.

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private var screenState = context.broadcastReceiverAsFlow(Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON)
        .map {
            !powerManager.isInteractive
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, !powerManager.isInteractive)


    init {
        lifecycle.whenResumed {
            screenState.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        return screenState.value
    }

}