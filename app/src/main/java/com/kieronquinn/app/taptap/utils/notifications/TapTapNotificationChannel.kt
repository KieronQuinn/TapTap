package com.kieronquinn.app.taptap.utils.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.taptap.R

sealed class TapTapNotificationChannel(
    open val notificationChannelId: TapTapNotificationChannelId,
    open val importance: Int,
    open val titleRes: Int,
    open val contentRes: Int
) {

    companion object {
        const val NOTIFICATION_ICON = R.drawable.ic_taptap_logo
    }

    object Background : TapTapNotificationChannel(
        TapTapNotificationChannelId.BACKGROUND,
        NotificationManager.IMPORTANCE_DEFAULT,
        R.string.tap_notification_channel_title,
        R.string.tap_notification_channel_description
    )

    object Action: TapTapNotificationChannel(
        TapTapNotificationChannelId.ACTION,
        NotificationManager.IMPORTANCE_HIGH,
        R.string.notification_channel_actions_title,
        R.string.notification_channel_actions_content
    )

    object Service: TapTapNotificationChannel(
        TapTapNotificationChannelId.SERVICE,
        NotificationManager.IMPORTANCE_HIGH,
        R.string.notification_channel_service_title,
        R.string.notification_channel_service_content
    )

    object Update: TapTapNotificationChannel(
        TapTapNotificationChannelId.UPDATE,
        NotificationManager.IMPORTANCE_HIGH,
        R.string.tap_update_notification_channel_title,
        R.string.tap_update_notification_channel_description
    )

    object Crash: TapTapNotificationChannel(
        TapTapNotificationChannelId.CRASH,
        NotificationManager.IMPORTANCE_HIGH,
        R.string.notification_channel_crash_title,
        R.string.notification_channel_crash_content
    )

    enum class TapTapNotificationChannelId {
        BACKGROUND, SERVICE, ACTION, UPDATE, CRASH
    }

    private fun createChannelIfNeeded(context: Context) {
        NotificationChannelCompat.Builder(notificationChannelId.name, importance).apply {
            setName(context.getString(titleRes))
            setDescription(context.getString(contentRes))
        }.build().also {
            NotificationManagerCompat.from(context).createNotificationChannel(it)
        }
    }

    fun createNotification(context: Context, options: (NotificationCompat.Builder) -> Unit): Notification {
        createChannelIfNeeded(context)
        return NotificationCompat.Builder(context, notificationChannelId.name).apply { options(this) }.build()
    }

    fun showNotification(context: Context, notificationId: TapTapNotificationId, options: (NotificationCompat.Builder) -> Unit) {
        val notification = createNotification(context, options)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId.ordinal, notification)
    }

    fun cancelNotifications(context: Context, vararg notificationIds: TapTapNotificationId) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationIds.forEach {
            notificationManager.cancel(it.ordinal)
        }
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}