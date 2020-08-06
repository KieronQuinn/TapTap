package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isAppLaunchable
import com.kieronquinn.app.taptap.utils.isPackageAssistant

abstract class ActionBase(context: Context) : Action(context, emptyList()) {
    override fun onProgress(var1: Int, var2: GestureSensor.DetectionProperties?) {
        if(var1 != 3) return;

        onTrigger()
    }
}