package com.kieronquinn.app.taptap.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService

class TapTapServiceStartReceiver: BroadcastReceiver() {

    companion object {
        private val ALLOWED_INTENT_ACTIONS = arrayOf(BuildConfig.BROADCAST_ACTION_START, Intent.ACTION_BOOT_COMPLETED)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(!ALLOWED_INTENT_ACTIONS.contains(intent.action)) return
        TapTapForegroundService.start(context)
    }

}