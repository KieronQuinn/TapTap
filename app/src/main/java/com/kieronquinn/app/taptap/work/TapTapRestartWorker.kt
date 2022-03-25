package com.kieronquinn.app.taptap.work

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService
import com.kieronquinn.app.taptap.utils.extensions.canUseContextHub
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.util.concurrent.TimeUnit

class TapTapRestartWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams), KoinComponent {

    companion object {

        private const val TAG = "tap_tap_restart_worker"
        private fun getWorkManager(context: Context) = WorkManager.getInstance(context)

        fun cancel(context: Context, workManager: WorkManager? = null) {
            (workManager ?: getWorkManager(context)).cancelAllWorkByTag(TAG)
        }

        fun enqueue(context: Context) {
            val workManager = getWorkManager(context)
            cancel(context, workManager)
            workManager.enqueue(
                PeriodicWorkRequestBuilder<TapTapRestartWorker>(Duration.ofHours(1))
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .addTag(TAG).build()
            )
        }

    }

    private val settings by inject<TapTapSettings>()

    override fun doWork(): Result {
        //Do not run if CHRE is enabled as it seems to mess with the timings and has little benefit
        if(settings.lowPowerMode.getSync() && applicationContext.canUseContextHub)
            return Result.success()
        TapTapForegroundService.stop(applicationContext)
        if (settings.serviceEnabled.getSync()) {
            TapTapForegroundService.start(applicationContext)
        }
        return Result.success()
    }

}