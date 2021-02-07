package com.kieronquinn.app.taptap.core.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.taptap.core.TapColumbusService
import com.kieronquinn.app.taptap.core.TapServiceContainer
import org.koin.android.ext.android.inject

class TapGestureAccessibilityService : AccessibilityService() {

    private val serviceContainer by inject<TapServiceContainer>()
    private val columbusService by inject<TapColumbusService>()

    override fun onCreate() {
        super.onCreate()
        sendBroadcast(Intent(TapAccessibilityService.KEY_ACCESSIBILITY_START).setPackage(packageName))
        serviceContainer.gestureAccessibilityService = this
        columbusService.ping()
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceContainer.gestureAccessibilityService = null
    }
}