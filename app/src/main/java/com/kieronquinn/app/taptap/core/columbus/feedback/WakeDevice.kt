package com.kieronquinn.app.taptap.core.columbus.feedback

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.ui.activities.UnlockDeviceActivity
import com.kieronquinn.app.taptap.ui.activities.WakeUpActivity
import com.kieronquinn.app.taptap.utils.extensions.legacySharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WakeDevice(private val context: Context) : FeedbackEffect {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private val sharedPrefs = context.legacySharedPreferences
    private val useLegacyMethod
        get() = sharedPrefs?.getBoolean("advanced_legacy_wake", false) ?: false
    private var requireUnlock
        get() = (context.applicationContext as? TapTapApplication)?.requireUnlock ?: false
        set(value) {
            (context.applicationContext as? TapTapApplication)?.requireUnlock = value
        }

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var2 != null && !var2.isHapticConsumed) {
            if((context.applicationContext as? TapTapApplication)?.disableWake == true){
                (context.applicationContext as? TapTapApplication)?.disableWake = false
            }else {
                wakeUpDevice()
            }
        }
    }

    @Suppress("DEPRECATION") //FLAG_KEEP_SCREEN_ON turns out to not actually be that reliable on some devices
    private fun wakeUpDevice(){
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(keyguardManager.isDeviceLocked){
            if(useLegacyMethod){
                context.startActivity(Intent(context, WakeUpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT))
                //Legacy wakeup also handles unlock
                requireUnlock = false
            }else {
                GlobalScope.launch {
                    val wakeLock = powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                        "taptap::wakeup"
                    )
                    wakeLock.acquire(5000)
                    delay(5000)
                    wakeLock.release()
                }
                if(requireUnlock){
                    context.startActivity(Intent(context, UnlockDeviceActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT))
                    requireUnlock = false
                }
            }
        }
    }
}