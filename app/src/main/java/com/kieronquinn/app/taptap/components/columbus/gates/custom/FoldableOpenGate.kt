package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.window.FoldingFeature
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.foldable.SidecarProvider

class FoldableOpenGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    private val sidecarProvider by lazy {
        try {
            SidecarProvider(context)
        }catch(e: Exception) {
            //Sidecar is not supported
            null
        }
    }

    override fun isBlocked(): Boolean {
        return sidecarProvider?.getDevicePosture() == FoldingFeature.STATE_FLIPPED
    }

}