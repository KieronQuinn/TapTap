package com.kieronquinn.app.taptap.components.columbus.gates.custom

import android.content.Context
import androidx.lifecycle.Lifecycle

class HeadsetInverseGate(
    serviceLifecycle: Lifecycle,
    context: Context
) : HeadsetGate(serviceLifecycle, context) {

    override fun isBlocked(): Boolean {
        return !super.isBlocked()
    }

}