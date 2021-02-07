package com.kieronquinn.app.taptap.core.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.taptap.core.TapColumbusService
import com.kieronquinn.app.taptap.core.TapServiceContainer
import org.koin.android.ext.android.inject
import java.lang.Exception

class TapAccessibilityService: AccessibilityService() {

    companion object {
        val KEY_ACCESSIBILITY_START = "accessibility_start"
        const val TAG = "TapAccessibilityService"
    }

    private val serviceContainer by inject<TapServiceContainer>()
    private val columbusService by inject<TapColumbusService>()

    private var currentPackageName: String = "android"

    var isNotificationShadeOpen = false
    var isQuickSettingsOpen = false

    private val notificationShadeAccessibilityDesc by lazy {
        val default = "Notification shade."
        var value = default
        try{
            packageManager.getResourcesForApplication("com.android.systemui").run {
                value = getString(getIdentifier("accessibility_desc_notification_shade", "string", "com.android.systemui"))
            }
        }catch (e: Exception){}
        value
    }

    private val quickSettingsAccessibilityDesc by lazy {
        val default = "Quick settings."
        var value = default
        try{
            packageManager.getResourcesForApplication("com.android.systemui").run {
                value = getString(getIdentifier("accessibility_desc_quick_settings", "string", "com.android.systemui"))
            }
        }catch (e: Exception){}
        value
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        sendBroadcast(Intent(KEY_ACCESSIBILITY_START).setPackage(packageName))
        serviceContainer.accessibilityService = this
        columbusService.ping()
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceContainer.accessibilityService = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        isNotificationShadeOpen = event.text?.firstOrNull() == notificationShadeAccessibilityDesc
        isQuickSettingsOpen = event.text?.firstOrNull() == quickSettingsAccessibilityDesc
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName?.toString() != currentPackageName) {
            if(event.packageName?.toString() == "android") return
            currentPackageName = event.packageName?.toString() ?: "android"
        }
    }

    override fun onInterrupt() {

    }

    fun getCurrentPackageName(): String {
        val rootNode = rootInActiveWindow ?: return currentPackageName
        val currentPackage = rootNode.packageName?.toString() ?: currentPackageName
        rootNode.recycle()
        return currentPackage
    }

}