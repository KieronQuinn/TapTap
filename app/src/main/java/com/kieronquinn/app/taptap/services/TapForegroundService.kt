package com.kieronquinn.app.taptap.services

import android.app.*
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.TapTapApplication
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.utils.*

class TapForegroundService : LifecycleService(), ServiceConnection, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TFS"
        private const val MESSAGE_START = 1001
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private val tapSharedComponent by lazy {
        TapSharedComponent(this)
    }

    private val application by lazy {
        applicationContext as TapTapApplication
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if(msg.arg1 == MESSAGE_START) {
                tapSharedComponent.startTap()
            }
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        Log.d(TAG, "onServiceConnected")
        tapSharedComponent.accessibilityService = application.accessibilityService.value!!
        application.accessibilityService.observe(this, Observer {
            if(it != null) {
                tapSharedComponent.accessibilityService = it
            }
        })
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = MESSAGE_START
            serviceHandler?.sendMessage(msg)
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.d(TAG, "onServiceDisconnected")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        HandlerThread("TapService", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
        Log.d(TAG, "onCreate")
        val intent = Intent(this, TapAccessibilityService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notificationManager = NotificationManagerCompat.from(this)
        val channelId = "background_notification"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.tap_notification_channel_title), NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.tap_notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
            .setAutoCancel(true)
            .build()
        startForeground(1, notification)
        Log.d(TAG, "startForeground called")
        return START_STICKY
    }

    fun getCurrentPackageName(): String {
        return tapSharedComponent.getCurrentPackageName()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == SHARED_PREFERENCES_KEY_SPLIT_SERVICE){
            if(!isSplitService){
                Log.d(TAG, "Stopping self")
                tapSharedComponent.stopTap()
                stopSelf()
            }
        }else if(key == SHARED_PREFERENCES_KEY_MAIN_SWITCH){
            if(!isMainEnabled){
                tapSharedComponent.stopTap()
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
        tapSharedComponent.stopTap()
    }

}