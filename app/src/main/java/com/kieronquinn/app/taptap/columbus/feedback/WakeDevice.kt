package com.kieronquinn.app.taptap.columbus.feedback

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.activities.WakeUpActivity


class WakeDevice(private val context: Context) : FeedbackEffect {

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var2 != null && !var2.isHapticConsumed) {
            if((context.applicationContext as? TapTapApplication)?.disableWake == true){
                (context.applicationContext as? TapTapApplication)?.disableWake = false
            }else {
                wakeUpDevice()
            }
        }
    }

    private fun wakeUpDevice(){
        Log.d("WakeDevice", "wake")
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(keyguardManager.isDeviceLocked){
            context.startActivity(Intent(context, WakeUpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT))
        }
    }
}