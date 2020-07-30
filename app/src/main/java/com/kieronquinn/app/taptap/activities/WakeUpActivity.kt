package com.kieronquinn.app.taptap.activities

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager

class WakeUpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("WakeDevice", "onCreate")
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

}