package com.kieronquinn.app.taptap.service.accessibility

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.utils.lifecycle.LifecycleAccessibilityService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import org.koin.android.ext.android.inject

class TapTapAccessibilityService: LifecycleAccessibilityService() {

    companion object {
        private val EVENT_TYPES_SHADE_OPEN = arrayOf(
            AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED,
            AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_APPEARED
        )
        private val EVENT_TYPES_SHADE_CLOSED = arrayOf(
            AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED
        )
    }

    private val router by inject<TapTapAccessibilityRouter>()
    private var currentPackageName = "android"

    private var isNotificationShadeOpen = false
    private var isQuickSettingsOpen = false

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

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launchWhenCreated {
            setupInputListener()
        }
        lifecycleScope.launchWhenCreated {
            router.onAccessibilityStarted()
        }
    }

    private suspend fun setupInputListener() {
        router.accessibilityInputBus.filterNot {
            it is TapTapAccessibilityRouter.AccessibilityInput.GestureInput
        }.collect {
            handleInput(it)
        }
    }

    private fun handleInput(accessibilityInput: TapTapAccessibilityRouter.AccessibilityInput) {
        when(accessibilityInput) {
            is TapTapAccessibilityRouter.AccessibilityInput.PerformGlobalAction -> {
                performGlobalAction(accessibilityInput.globalActionId)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event == null) return
        if(event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val isNotificationShade = event.text?.firstOrNull() == notificationShadeAccessibilityDesc
        val isQuickSettings = event.text?.firstOrNull() == quickSettingsAccessibilityDesc
        var currentPackageName = this.currentPackageName
        Log.d("NEA", "Change type ${event.contentChangeTypes}")
        if(event.packageName?.toString() != currentPackageName) {
            if(event.packageName?.toString() != "android") {
                currentPackageName = event.packageName?.toString() ?: "android"
            }
        }
        lifecycleScope.launchWhenCreated {
            if(this@TapTapAccessibilityService.currentPackageName != currentPackageName){
                this@TapTapAccessibilityService.currentPackageName = currentPackageName
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.AppOpen(currentPackageName))
            }
            if(isNotificationShade && EVENT_TYPES_SHADE_OPEN.contains(event.contentChangeTypes)){
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.NotificationShadeState(true))
            }
            if(isNotificationShade && EVENT_TYPES_SHADE_CLOSED.contains(event.contentChangeTypes)){
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.NotificationShadeState(false))
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.QuickSettingsShadeState(false))
            }
            if(isQuickSettings && EVENT_TYPES_SHADE_OPEN.contains(event.contentChangeTypes)){
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.QuickSettingsShadeState(true))
            }
            if(isQuickSettings && EVENT_TYPES_SHADE_CLOSED.contains(event.contentChangeTypes)){
                router.postOutput(TapTapAccessibilityRouter.AccessibilityOutput.QuickSettingsShadeState(false))
            }
        }
    }

    override fun onInterrupt() {
        //no-op
    }

}