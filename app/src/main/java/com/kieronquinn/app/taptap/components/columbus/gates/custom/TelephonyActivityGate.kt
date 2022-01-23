package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import android.telephony.TelephonyManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.onCallStateChanged
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TelephonyActivityGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context) {

    private var isCallBlocked = context.onCallStateChanged()
        .map { telephonyManager.callState == 2 }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    private val telephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    init {
        lifecycleScope.launchWhenCreated {
            isCallBlocked.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        return isCallBlocked.value
    }

}