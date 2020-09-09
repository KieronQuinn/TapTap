package com.kieronquinn.app.taptap.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.workers.RestartWorker

class TapAccessibilityService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TAS"
        val KEY_ACCESSIBILITY_START = "accessibility_start"
    }

    private val application by lazy {
        applicationContext as TapTapApplication
    }

    private var currentPackageName: String = "android"

    private var tapSharedComponent: TapSharedComponent? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if(isMainEnabled){
            startTap()
        }
    }

    private fun startTap(){
        application.accessibilityService.postValue(this)
        if(isSplitService) {
            startSplitService()
        }else{
            tapSharedComponent = TapSharedComponent(this).apply {
                accessibilityService = this@TapAccessibilityService
                startTap()
            }
        }
        if(isRestartEnabled){
            RestartWorker.queueRestartWorker(this)
        }
        sendBroadcast(Intent(KEY_ACCESSIBILITY_START).setPackage(packageName))
    }

    private fun stopTap(){
        if(!isSplitService) {
            tapSharedComponent?.stopTap()
            tapSharedComponent = null
        }
        RestartWorker.clearRestartWorker(this)
    }

    private fun startSplitService(){
        val intent = Intent(this, TapForegroundService::class.java)
        stopService(intent)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        application.accessibilityService.postValue(null)
        //Stop the service to prevent listeners still being attached
        tapSharedComponent?.stopTap()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName?.toString() != currentPackageName) {
            Log.d("TASP", event.packageName.toString())
            if(event.packageName?.toString() == "android") return
            currentPackageName = event.packageName?.toString() ?: "android"
        }
    }

    fun getCurrentPackageName(): String {
        return currentPackageName
    }

    fun restartService(){
        //We can't actually restart the accessibility service, but we can restart the Tap component and the foreground service (if in use)
        if(isSplitService) {
            startSplitService()
        }else{
            runOnUiThread {
                tapSharedComponent?.stopTap()
                tapSharedComponent?.startTap()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == SHARED_PREFERENCES_KEY_SPLIT_SERVICE && isMainEnabled){
            if(isSplitService){
                tapSharedComponent?.stopTap()
                tapSharedComponent = null
                startSplitService()
            }else{
                if(tapSharedComponent == null){
                    tapSharedComponent = TapSharedComponent(this).apply {
                        accessibilityService = this@TapAccessibilityService
                        startTap()
                    }
                }else{
                    tapSharedComponent?.startTap()
                }
            }
        }else if(key == SHARED_PREFERENCES_KEY_MAIN_SWITCH){
            if(isMainEnabled){
                startTap()
            }else{
                stopTap()
            }
        }else if(key == SHARED_PREFERENCES_KEY_RESTART_SERVICE){
            if(isRestartEnabled){
                RestartWorker.queueRestartWorker(this)
            }else{
                RestartWorker.clearRestartWorker(this)
            }
        }
    }
}