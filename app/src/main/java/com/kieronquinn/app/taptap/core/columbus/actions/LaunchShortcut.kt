package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.deserialize

class LaunchShortcut(context: Context, private val launchIntentString: String, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        val currentPackage = accessibilityService.getCurrentPackageName()
        try {
            val launchIntent = Intent().apply {
                deserialize(launchIntentString)
            }
            val launchActivity = context.packageManager.queryIntentActivities(launchIntent, 0)
            if(launchActivity.isEmpty()) return false
            launchActivity.forEach {
                if(it.activityInfo.packageName == currentPackage) return false
            }
            return super.isAvailable()
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }

    override fun onTrigger() {
        super.onTrigger()
        try {
            val launchIntent = Intent().apply {
                deserialize(launchIntentString)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
            //Special case for CALL_PHONE required permissions (it'd be a really stupid idea for someone to actually *want* that action though...)
            if(e.message?.contains("android.permission.CALL_PHONE") == true){
                Toast.makeText(context, context.getString(R.string.call_phone_permission_toast), Toast.LENGTH_LONG).show()
            }
        }
    }


}