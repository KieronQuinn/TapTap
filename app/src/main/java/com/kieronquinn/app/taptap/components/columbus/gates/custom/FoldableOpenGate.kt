package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.utils.foldable.FoldableProvider

class FoldableOpenGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    private val sidecarProvider by lazy {
        FoldableProvider.getProvider(context)
    }

    override fun isBlocked(): Boolean {
        return sidecarProvider?.isClosed() == false
    }

}