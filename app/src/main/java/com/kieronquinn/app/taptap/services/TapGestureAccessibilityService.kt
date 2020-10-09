package com.kieronquinn.app.taptap.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.taptap.TapTapApplication

class TapGestureAccessibilityService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        sendBroadcast(Intent(TapAccessibilityService.KEY_ACCESSIBILITY_START).setPackage(packageName))
        (application as TapTapApplication).run {
            gestureAccessibilityService.postValue(this@TapGestureAccessibilityService)
        }
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as TapTapApplication).run {
            gestureAccessibilityService.postValue(null)
        }
    }
}