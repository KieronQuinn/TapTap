package com.kieronquinn.app.taptap.service.foreground

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.columbus.ColumbusServiceWrapper
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.ColumbusServiceSettings
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.columbus.feedback.custom.HapticClickFeedback
import com.kieronquinn.app.taptap.components.columbus.feedback.custom.WakeDeviceFeedback
import com.kieronquinn.app.taptap.components.columbus.sensors.ServiceEventEmitter
import com.kieronquinn.app.taptap.components.service.TapTapServiceRouter
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.utils.extensions.canUseContextHub
import com.kieronquinn.app.taptap.utils.extensions.isNativeColumbusEnabled
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.extensions.startForegroundCompat
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import com.kieronquinn.app.taptap.work.TapTapRestartWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.error.ClosedScopeException
import rikka.shizuku.ShizukuProvider

class TapTapForegroundService : LifecycleService(), KoinScopeComponent {

    companion object {

        const val ACTION_SERVICE_UPDATE = "${BuildConfig.APPLICATION_ID}.SERVICE_UPDATE"
        private const val KEY_IS_RESTART = "is_restart"

        private fun getIntent(context: Context, isRestart: Boolean): Intent {
            return Intent(context, TapTapForegroundService::class.java).apply {
                if (isRestart) {
                    putExtra(KEY_IS_RESTART, true)
                }
            }
        }

        fun start(context: Context, isRestart: Boolean = false) {
            val intent = getIntent(context, isRestart)
            context.stopService(intent)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = getIntent(context, false)
            context.stopService(intent)
        }

        fun isRunning(context: Context): Boolean {
            return context.isServiceRunning(TapTapForegroundService::class.java)
        }

    }

    override val scope by lazy {
        createScope(this)
    }

    private val columbusSettings by inject<ColumbusServiceSettings>()
    private val columbusWrapper by inject<ColumbusServiceWrapper>()
    private val settings by inject<TapTapSettings>()
    private val serviceEventEmitter by inject<ServiceEventEmitter>()
    private val actionsRepository by inject<ActionsRepository>()
    private val serviceContainer by inject<TapTapShizukuServiceRepository>()
    private val rootServiceContainer by inject<TapTapRootServiceRepository>()
    private val gatesRepository by inject<GatesRepository>()
    private val serviceRouter by inject<TapTapServiceRouter>()

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat(TapTapNotificationId.BACKGROUND.ordinal, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycle.whenCreated {
            listenForStartOrFail(intent?.getBooleanExtra(KEY_IS_RESTART, false) ?: false)
            startService()
        }
        TapTapRestartWorker.cancel(this)
        if(settings.advancedAutoRestart.getSync()){
            TapTapRestartWorker.enqueue(this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun startService() {
        val actions = actionsRepository.getSavedDoubleTapActions().mapNotNull {
            actionsRepository.createTapTapAction(
                it,
                false,
                this@TapTapForegroundService,
                lifecycle,
                serviceContainer,
                rootServiceContainer
            )
        }
        val tripleActions = if(!settings.actionsTripleTapEnabled.get()) {
            emptyList()
        }else{
            actionsRepository.getSavedTripleTapActions().mapNotNull {
                actionsRepository.createTapTapAction(
                    it,
                    true,
                    this@TapTapForegroundService,
                    lifecycle,
                    serviceContainer,
                    rootServiceContainer
                )
            }
        }
        val gates = gatesRepository.getSavedGates().filter { it.enabled }.mapNotNull {
            gatesRepository.createTapTapGate(
                it,
                this@TapTapForegroundService,
                lifecycle
            )
        }
        with(columbusSettings) {
            setLifecycleOwner(this@TapTapForegroundService)
            setUseContextHub(canUseContextHub && settings.lowPowerMode.get())
            setUseContextHubLogging(isNativeColumbusEnabled())
            setTripleTapEnabled(tripleActions.isNotEmpty())
            setActions(actions)
            setTripleTapActions(tripleActions)
            setGates(gates)
            val feedbackEffects = ArrayList<TapTapFeedbackEffect>()
            if (settings.feedbackVibrate.get()) {
                feedbackEffects.add(HapticClickFeedback(lifecycle, this@TapTapForegroundService))
            }
            if (settings.feedbackWakeDevice.get()) {
                feedbackEffects.add(WakeDeviceFeedback(lifecycle, this@TapTapForegroundService, false))
            }
            setFeedbackEffects(feedbackEffects)
            setTapModel(settings.columbusTapModel.get())
        }
        columbusWrapper.startService()
        serviceRouter.onServiceStarted()
    }

    private fun listenForStartOrFail(isRestart: Boolean) = lifecycle.whenCreated {
        showStartingNotification(isRestart)
        serviceEventEmitter.serviceEvent.take(1).collect {
            sendUpdateBroadcast()
            when (it) {
                is ServiceEventEmitter.ServiceEvent.Started -> {
                    TapTapNotificationChannel.Service.cancelNotifications(
                        this@TapTapForegroundService,
                        TapTapNotificationId.SERVICE
                    )
                    setupServiceErrorListener()
                }
                is ServiceEventEmitter.ServiceEvent.Failed -> {
                    showErrorNotification(false, it.reason.contentRes)
                    stopSelf()
                }
            }
        }
    }

    private fun showStartingNotification(isRestart: Boolean) {
        TapTapNotificationChannel.Service.showNotification(
            this@TapTapForegroundService,
            TapTapNotificationId.SERVICE
        ) {
            val content = getString(R.string.notification_service_loading_content)
            it.setOngoing(true)
            it.setProgress(100, 0, true)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            val title = if (isRestart) {
                R.string.notification_service_loading_title_restart
            } else {
                R.string.notification_service_loading_title
            }
            it.setContentTitle(getString(title))
            it.setContentText(content)
            it.setContentIntent(
                PendingIntent.getActivity(
                    this,
                    TapTapNotificationIntentId.START_CLICK.ordinal,
                    packageManager.getLaunchIntentForPackage(packageName)?.apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.priority = NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun showErrorNotification(running: Boolean, contentRes: Int) {
        val shizukuIntent =
            packageManager.getLaunchIntentForPackage(ShizukuProvider.MANAGER_APPLICATION_ID)
        val retryIntent = Intent(BuildConfig.BROADCAST_ACTION_START).apply {
            `package` = packageName
        }.run {
            PendingIntent.getBroadcast(
                this@TapTapForegroundService,
                TapTapNotificationIntentId.ERROR_RETRY_CLICK.ordinal,
                this,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        TapTapNotificationChannel.Service.showNotification(
            this@TapTapForegroundService,
            TapTapNotificationId.SERVICE
        ) {
            val content = getString(contentRes)
            val title = if (running) {
                getString(R.string.notification_service_running_error_title)
            } else {
                getString(R.string.notification_service_error_title)
            }
            it.setOngoing(false)
            it.setAutoCancel(true)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(title)
            it.setContentText(content)
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.priority = NotificationCompat.PRIORITY_HIGH
            it.addAction(
                0,
                getString(R.string.notification_service_error_action_retry),
                retryIntent
            )
            if (shizukuIntent != null) {
                it.setContentIntent(
                    PendingIntent.getActivity(
                        this@TapTapForegroundService,
                        TapTapNotificationIntentId.ERROR_CLICK.ordinal,
                        shizukuIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
    }

    private suspend fun setupServiceErrorListener() {
        serviceEventEmitter.serviceEvent.filter { it is ServiceEventEmitter.ServiceEvent.Failed }
            .collect {
                showErrorNotification(
                    true,
                    (it as ServiceEventEmitter.ServiceEvent.Failed).reason.contentRes
                )
                sendUpdateBroadcast()
                stopSelf()
            }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                putExtra(
                    Settings.EXTRA_CHANNEL_ID,
                    TapTapNotificationChannel.Background.notificationChannelId.name
                )
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            } else {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$packageName")
            }
        }.run {
            PendingIntent.getActivity(
                this@TapTapForegroundService,
                TapTapNotificationIntentId.HIDE_CLICK.ordinal,
                this,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        return TapTapNotificationChannel.Background.createNotification(this) {
            val content = getString(R.string.tap_notification_content)
            it.setOngoing(true)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(getString(R.string.tap_notification_title))
            it.setContentText(content)
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setContentIntent(intent)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.priority = NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun sendUpdateBroadcast() {
        Intent(ACTION_SERVICE_UPDATE).apply {
            `package` = packageName
        }.also {
            sendBroadcast(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.close()
        //Out of regular scope
        GlobalScope.launch {
            try {
                serviceRouter.onServiceStopped()
            }catch (e: ClosedScopeException){
                //App has been killed
            }
        }
    }

}