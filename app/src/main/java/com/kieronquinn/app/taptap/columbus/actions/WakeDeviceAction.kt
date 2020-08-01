package com.kieronquinn.app.taptap.columbus.actions

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.WakeUpActivity
import com.kieronquinn.app.taptap.utils.isAppLaunchable

class WakeDeviceAction(context: Context) : ActionBase(context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    override fun isAvailable(): Boolean {
        return !powerManager.isInteractive
    }

    override fun onTrigger() {
        super.onTrigger()
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(keyguardManager.isDeviceLocked){
            context.startActivity(Intent(context, WakeUpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT))
        }
    }


}