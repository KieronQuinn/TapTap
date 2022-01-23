package com.google.android.columbus

import android.content.Context
import android.os.PowerManager

class PowerManagerWrapper(context: Context) {

    inner class WakeLockWrapper(private val wakeLock: PowerManager.WakeLock) {
        fun acquire(timeout: Long) {
            wakeLock.acquire(timeout)
        }
    }

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun isInteractive(): Boolean {
        return powerManager.isInteractive
    }

    fun newWakeLock(levelAndFlags: Int, tag: String): WakeLockWrapper {
        return WakeLockWrapper(powerManager.newWakeLock(levelAndFlags, tag))
    }

}