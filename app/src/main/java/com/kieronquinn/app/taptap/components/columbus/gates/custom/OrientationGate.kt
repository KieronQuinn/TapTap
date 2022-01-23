package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.components.columbus.gates.PassiveGate
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate

class OrientationGate(
    serviceLifecycle: Lifecycle,
    context: Context,
    private val blockedOrientation: Int
) : TapTapGate(serviceLifecycle, context), PassiveGate {

    override fun isBlocked(): Boolean {
        return context.resources.configuration.orientation == blockedOrientation
    }

}