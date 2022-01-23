package com.kieronquinn.app.taptap.work

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.update.UpdateRepository
import com.kieronquinn.app.taptap.ui.activities.MainActivity
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class TapTapUpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams), KoinComponent {

    companion object {
        private const val UPDATE_CHECK_WORK_TAG = "tap_tap_update_check"
        private const val UPDATE_CHECK_HOUR = 12L

        private fun clearCheckWorker(context: Context){
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(UPDATE_CHECK_WORK_TAG)
        }

        fun queueCheckWorker(context: Context){
            clearCheckWorker(context)
            val checkWorker = Builder().build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(UPDATE_CHECK_WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, checkWorker)
        }
    }

    private val updateRepository by inject<UpdateRepository>()
    private val settings by inject<TapTapSettings>()

    override fun doWork(): Result {
        GlobalScope.launch {
            //Reject if internet is disabled
            if(!settings.internetAllowed.get()) return@launch
            //Reject if background internet is disabled
            if(!settings.backgroundUpdateCheck.get()) return@launch
            //Reject if there's no update
            val update = updateRepository.getUpdate() ?: return@launch
            TapTapNotificationChannel.Update.showNotification(
                applicationContext,
                TapTapNotificationId.UPDATE
            ){
                val content = applicationContext.getString(R.string.tap_update_notification_content, BuildConfig.VERSION_NAME, update.versionName)
                val title = applicationContext.getString(R.string.tap_update_notification_title)
                it.setOngoing(false)
                it.setAutoCancel(true)
                it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
                it.setContentTitle(title)
                it.setContentText(content)
                it.setCategory(Notification.CATEGORY_SERVICE)
                it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
                it.priority = NotificationCompat.PRIORITY_HIGH
                it.setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        TapTapNotificationIntentId.UPDATE_CLICK.ordinal,
                        Intent(applicationContext, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
        return Result.success()
    }

    class Builder {
        fun build() : PeriodicWorkRequest {
            val delay = if (LocalDateTime.now().hour < UPDATE_CHECK_HOUR) {
                Duration.between(ZonedDateTime.now(), ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusHours(UPDATE_CHECK_HOUR)).toMinutes()
            } else {
                Duration.between(ZonedDateTime.now(), ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusDays(1).plusHours(UPDATE_CHECK_HOUR)).toMinutes()
            }

            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return PeriodicWorkRequest.Builder(TapTapUpdateCheckWorker::class.java, 24, TimeUnit.HOURS).addTag(UPDATE_CHECK_WORK_TAG)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()
        }
    }

}