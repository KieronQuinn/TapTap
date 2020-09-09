package com.kieronquinn.app.taptap.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.utils.isRestartEnabled
import java.util.concurrent.TimeUnit

class RestartWorker(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

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
        val application = context.applicationContext as? TapTapApplication ?: return Result.failure()
        val accessibilityService = application.accessibilityService.value ?: return Result.failure()
        accessibilityService.restartService()
        if(context.isRestartEnabled){
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