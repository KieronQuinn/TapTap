package com.kieronquinn.app.taptap.components.columbus.feedback.custom

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.activities.UnlockDeviceActivity
import com.kieronquinn.app.taptap.ui.activities.WakeUpActivity
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class WakeDeviceFeedback(serviceLifecycle: Lifecycle, private val context: Context, private val unlock: Boolean) :
    TapTapFeedbackEffect(serviceLifecycle) {

    companion object {
        private const val TAG = "TapTap:WakeDeviceFeedback"
    }

    private val settings by inject<TapTapSettings>()

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        if(settings.advancedLegacyWake.get()) {
            context.startActivity(Intent(context, WakeUpActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(WakeUpActivity.EXTRA_UNLOCK, unlock)
            })
        }else{
            lifecycleScope.launch {
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    TAG
                )
                wakeLock.acquire(5000)
                delay(5000)
                wakeLock.release()
            }
            if(unlock) {
                context.startActivity(Intent(context, UnlockDeviceActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    /**
     *  Android 11 introduces restrictions that prevent activity launches from a foreground service,
     *  so we need the draw over other apps permission
     */
    private fun showOverlayNotificationIfNeeded(): Boolean {
        return if(!Settings.canDrawOverlays(context)) {
            showOverlayNotification()
            true
        }else false
    }

    private fun showOverlayNotification() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        }
        showActionNotification(
            intent,
            R.string.notification_action_overlay_error_title,
            R.string.notification_action_overlay_error_content
        )
    }

    private fun showActionNotification(clickIntent: Intent, @StringRes titleRes: Int, @StringRes contentRes: Int) {
        TapTapNotificationChannel.Action.showNotification(context, TapTapNotificationId.ACTION) {
            val content = context.getString(contentRes)
            it.setOngoing(false)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(context.getString(titleRes))
            it.setContentText(content)
            it.setAutoCancel(true)
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.setContentIntent(PendingIntent.getActivity(context, TapTapNotificationIntentId.ACTION_CLICK.ordinal, clickIntent, PendingIntent.FLAG_IMMUTABLE))
            it.priority = NotificationCompat.PRIORITY_HIGH
        }
    }

}