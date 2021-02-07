package com.kieronquinn.app.taptap.core.workers

import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.core.services.TapForegroundService
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class RestartWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams), KoinComponent {

    private val tapSharedPreferences by inject<TapSharedPreferences>()

    companion object {
        private const val RESTART_SERVICE_WORK_TAG = "restart_service"

        fun queueRestartWorker(context: Context){
            clearRestartWorker(context)
            val restartWork = Builder().build(RESTART_SERVICE_WORK_TAG, 1, TimeUnit.HOURS)
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(restartWork)
        }

        fun clearRestartWorker(context: Context){
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(RESTART_SERVICE_WORK_TAG)
        }
    }

    override fun doWork(): Result {
        if(!context.isServiceRunning(TapForegroundService::class.java)){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, TapForegroundService::class.java))
            }else{
                context.startService(Intent(context, TapForegroundService::class.java))
            }
        }
        if(tapSharedPreferences.isRestartEnabled){
            queueRestartWorker(context)
        }
        return Result.success()
    }

    class Builder {
        fun build(tag: String, timeDelay: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(RestartWorker::class.java)
                .setInitialDelay(timeDelay, timeUnit)
                .addTag(tag)
                .build()
        }
    }

}