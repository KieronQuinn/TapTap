package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.Intent
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.isAppLaunchable
import com.kieronquinn.app.taptap.utils.isPackageAssistant

class LaunchAssistant(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return !context.isPackageAssistant(accessibilityService.getCurrentPackageName()) && super.isAvailable()
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