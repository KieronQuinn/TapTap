package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UsbStateGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context) {

    companion object {
        private val USB_EXTRAS = arrayOf("connected", "host_connected")
    }

    private var usbState = context.broadcastReceiverAsFlow("android.hardware.usb.action.USB_STATE")
        .map { intent ->
            USB_EXTRAS.any { intent.getBooleanExtra(it, false) }
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycle.whenCreated {
            usbState.collect {
                notifyListeners()
            }
        }
    }

    override fun isBlocked(): Boolean {
        return usbState.value
    }

}