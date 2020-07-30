package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.Intent
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isAppLaunchable
import com.kieronquinn.app.taptap.utils.isPackageAssistant

class LaunchAssistant(context: Context) : Action(context, emptyList()) {

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return !context.isPackageAssistant(accessibilityService.getCurrentPackageName())
    }

    override fun onProgress(var1: Int, var2: GestureSensor.DetectionProperties?) {
        if (var1 == 3) {
            onTrigger()
        }
    }

    override fun onTrigger() {
        super.onTrigger()
        try {
            val launchIntent = Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


}