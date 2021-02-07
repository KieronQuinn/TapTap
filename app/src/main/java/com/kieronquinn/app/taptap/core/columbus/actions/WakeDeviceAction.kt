package com.kieronquinn.app.taptap.core.columbus.actions

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.kieronquinn.app.taptap.ui.activities.WakeUpActivity
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.ui.activities.UnlockDeviceActivity
import com.kieronquinn.app.taptap.utils.extensions.legacySharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WakeDeviceAction(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    private val sharedPrefs = context.legacySharedPreferences
    private val useLegacyMethod
        get() = sharedPrefs?.getBoolean("advanced_legacy_wake", false) ?: false

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    override fun isAvailable(): Boolean {
        return !powerManager.isInteractive && super.isAvailable()
    }

    override fun onTrigger() {
        super.onTrigger()
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(keyguardManager.isDeviceLocked){
            if(useLegacyMethod){
                context.startActivity(Intent(context, WakeUpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT).apply {
                    putExtra("unlock", true)
                })
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
                context.startActivity(Intent(context, UnlockDeviceActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT))
            }
        }
    }


}