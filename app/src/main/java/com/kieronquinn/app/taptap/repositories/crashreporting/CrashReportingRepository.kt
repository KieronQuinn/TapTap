package com.kieronquinn.app.taptap.repositories.crashreporting

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.activities.CrashReportingActivity
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId

interface CrashReportingRepository {

    fun setEnabled(enabled: Boolean)

}

class CrashReportingRepositoryImpl(
    private val applicationContext: Context
): CrashReportingRepository, Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private var enabled = false

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if(enabled){
            showCrashNotification(throwable)
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun showCrashNotification(throwable: Throwable) {
        TapTapNotificationChannel.Crash.showNotification(applicationContext, TapTapNotificationId.CRASH) {
            val content = applicationContext.getString(R.string.notification_crash_content)
            it.setOngoing(false)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(applicationContext.getString(R.string.notification_crash_title))
            it.setContentText(content)
            it.setAutoCancel(true)
            it.setCategory(Notification.CATEGORY_ERROR)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.setContentIntent(getReportActivityPendingIntent(throwable, applicationContext))
            it.priority = NotificationCompat.PRIORITY_HIGH
        }
    }

    private fun getReportActivityPendingIntent(throwable: Throwable, context: Context): PendingIntent {
        val intent = Intent(context, CrashReportingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(CrashReportingActivity.KEY_EXCEPTION, Log.getStackTraceString(throwable))
        }
        return PendingIntent.getActivity(
            context,
            TapTapNotificationIntentId.CRASH_CLICK.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}