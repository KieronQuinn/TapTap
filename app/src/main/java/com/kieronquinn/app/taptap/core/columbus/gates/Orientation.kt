package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import com.google.android.systemui.columbus.gates.Gate

class Orientation(context: Context, private val blockedOrientation: Int) : Gate(context) {

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        return context.resources.configuration.orientation == blockedOrientation
    }

}