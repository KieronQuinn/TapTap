package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isAppLaunchable

class LaunchApp(context: Context, private val appPackageName: String) : Action(context, emptyList()) {

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return context.isAppLaunchable(appPackageName) && accessibilityService.getCurrentPackageName() != appPackageName
    }

    override fun onProgress(var1: Int, var2: GestureSensor.DetectionProperties?) {
        if (var1 == 3) {
            onTrigger()
        }
    }

    override fun onTrigger() {
        super.onTrigger()
        val packageManager = context.packageManager
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


}