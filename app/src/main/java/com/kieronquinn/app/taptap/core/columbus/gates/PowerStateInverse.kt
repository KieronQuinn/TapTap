package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.google.android.systemui.columbus.gates.Gate

/*
    This is required instead of just cloning the other PowerState class and adding an inverse option as the WakefullnessLifecycle doesn't seem to work for inverse
    It's slower to react than the WakefullnessReceiver, so it's not replacing the original method
 */

class PowerStateInverse(context: Context) : Gate(context) {

    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val powerStateReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actions = arrayOf(Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON)
            if(actions.contains(intent?.action)){
                notifyListener()
            }
        }
    }

    override fun isBlocked(): Boolean = powerManager.isInteractive

    override fun onActivate() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(powerStateReceiver, filter)
    }

    override fun onDeactivate() {
        context.unregisterReceiver(powerStateReceiver)
    }
}