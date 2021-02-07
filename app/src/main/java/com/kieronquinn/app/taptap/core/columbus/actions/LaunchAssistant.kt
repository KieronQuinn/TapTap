package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.content.Intent
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.isPackageAssistant

class LaunchAssistant(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

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