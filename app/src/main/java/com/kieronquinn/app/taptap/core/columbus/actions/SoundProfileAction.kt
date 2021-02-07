package com.kieronquinn.app.taptap.core.columbus.actions

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.kieronquinn.app.taptap.models.WhenGateInternal

class SoundProfileAction(context: Context, whenList : List<WhenGateInternal>) : ActionBase(context, whenList) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentProfileAction = audioManager.ringerMode

    private val isNotificationAccessGranted: Boolean by lazy {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.isNotificationPolicyAccessGranted
    }

    override fun isAvailable(): Boolean {
        return isNotificationAccessGranted && super.isAvailable()
    }

    override fun onTrigger() {
        super.onTrigger()
        currentProfileAction = audioManager.ringerMode
        when (currentProfileAction) {
            0 -> {
                audioManager.ringerMode = 1
            }
            1 -> {
                audioManager.ringerMode = 2
            }
            2 -> {
                audioManager.ringerMode = 0
            }
        }
    }
}