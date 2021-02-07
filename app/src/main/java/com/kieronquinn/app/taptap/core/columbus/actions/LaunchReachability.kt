package com.kieronquinn.app.taptap.core.columbus.actions

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.ui.activities.ReachabilityActivity
import com.kieronquinn.app.taptap.models.WhenGateInternal

class LaunchReachability(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    override val requiresUnlock: Boolean = true

    companion object {
        const val INTENT_ACTION_START_SPLIT_SCREEN = "ACTION_START_SPLIT_SCREEN"
        const val INTENT_ACTION_SHOW_NOTIFICATIONS = "ACTION_SHOW_NOTIFICATIONS"
        const val INTENT_ACTION_SHOW_QUICK_SETTINGS = "ACTION_SHOW_QUICK_SETTINGS"
        const val INTENT_ACTION_ENDING = "ACTION_ENDING"
    }

    private val receiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            context as TapAccessibilityService
            context.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
            context.unregisterReceiver(this)
        }
    }

    private val buttonReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            context as TapAccessibilityService
            when(intent?.action){
                INTENT_ACTION_SHOW_NOTIFICATIONS -> context.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                INTENT_ACTION_SHOW_QUICK_SETTINGS -> context.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                INTENT_ACTION_ENDING -> context.unregisterReceiver(this)
            }
        }
    }

    override fun onTrigger() {
        super.onTrigger()
        val accessibilityService = context as TapAccessibilityService
        if(ReachabilityActivity.isRunning){
            //Just end split
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        }else {
            val intent = Intent(context, ReachabilityActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                putExtra(ReachabilityActivity.KEY_PACKAGE_NAME, accessibilityService.getCurrentPackageName())
            }
            context.registerReceiver(receiver, IntentFilter(INTENT_ACTION_START_SPLIT_SCREEN))
            context.registerReceiver(buttonReceiver, IntentFilter(INTENT_ACTION_SHOW_NOTIFICATIONS))
            context.registerReceiver(buttonReceiver, IntentFilter(INTENT_ACTION_SHOW_QUICK_SETTINGS))
            context.registerReceiver(buttonReceiver, IntentFilter(INTENT_ACTION_ENDING))
            context.startActivity(intent)
        }
    }


}