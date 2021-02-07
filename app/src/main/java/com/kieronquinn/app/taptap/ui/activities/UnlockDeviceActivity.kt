package com.kieronquinn.app.taptap.ui.activities

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.kieronquinn.app.taptap.TapTapApplication

class UnlockDeviceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

}