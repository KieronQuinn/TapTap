package com.kieronquinn.app.taptap.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapColumbusService
import com.kieronquinn.app.taptap.core.TapServiceContainer
import org.koin.android.ext.android.inject

class TapForegroundService: Service() {

    companion object {
        private const val MESSAGE_START = 1001
        private const val TAG = "TapForegroundService"
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private val serviceContainer by inject<TapServiceContainer>()
    private val columbusService by inject<TapColumbusService>()

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if(msg.arg1 == MESSAGE_START) {
                //tapSharedComponent.startTap()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        columbusService.ping()
        val notificationManager = NotificationManagerCompat.from(this)
        val channelId = "background_notification"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.tap_notification_channel_title), NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.tap_notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
        val clickIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_taptap_logo)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentTitle(getString(R.string.tap_notification_title))
            .setContentText(getString(R.string.tap_notification_content))
            .setContentIntent(PendingIntent.getActivity(this, 0, clickIntent, 0))
            .setAutoCancel(true)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        serviceContainer.foregroundService = this
        HandlerThread("TapService", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        serviceContainer.foregroundService = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}