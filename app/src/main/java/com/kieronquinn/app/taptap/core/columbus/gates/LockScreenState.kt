package com.kieronquinn.app.taptap.core.columbus.gates

import android.app.KeyguardManager
import android.content.Context
import com.google.android.systemui.columbus.gates.PowerState
import com.kieronquinn.app.taptap.utils.wakefulnessLifecycle

class LockScreenState(context: Context) : PowerState(context, wakefulnessLifecycle) {

    private val keyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    override fun isBlocked(): Boolean {
        return keyguardManager.isKeyguardLocked && !super.isBlocked()
    }

    override fun onActivate() {}
    override fun onDeactivate() {}

}