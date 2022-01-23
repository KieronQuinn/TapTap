package com.kieronquinn.app.taptap.components.columbus.actions

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.actions.Action
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.gates.Gate
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.feedback.custom.WakeDeviceFeedback
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.getAccessibilityIntent
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.extensions.runOnDestroy
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import org.koin.core.component.KoinComponent
import rikka.shizuku.ShizukuProvider

/**
 *  [Action] that is able to trigger on triple tap. All custom actions should extend from this.
 *
 *  Also implements [Lifecycle], to allow cleaning up when the lifecycle is destroyed (service dies)
 */
abstract class TapTapAction(
    private val serviceLifecycle: Lifecycle,
    private val context: Context,
    private val whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>,
    requiresWake: Boolean = false,
    requiresUnlock: Boolean = false
) : Action(context, effects.addWakeUnlockIfRequired(requiresWake, requiresUnlock, serviceLifecycle, context)), LifecycleOwner, KoinComponent {

    companion object {
        private fun Set<FeedbackEffect>.addWakeUnlockIfRequired(wake: Boolean, unlock: Boolean, serviceLifecycle: Lifecycle, context: Context): Set<FeedbackEffect> {
            return if(wake){
                this.plus(WakeDeviceFeedback(serviceLifecycle, context, unlock))
            }else this
        }
    }

    private val whenGateListener = object: Gate.Listener {
        override fun onGateChanged(gate: Gate) {
            notifyListeners()
        }
    }

    init {
        serviceLifecycle.runOnDestroy {
            listeners.clear()
            whenGates.forEach {
                it.unregisterListener(whenGateListener)
            }
            setAvailable(false)
            onDestroy()
        }
        whenGates.forEach {
            it.registerListener(whenGateListener)
        }
    }

    final override fun onGestureDetected(
        flags: Int,
        detectionProperties: GestureSensor.DetectionProperties?
    ) {
        super.onGestureDetected(flags, detectionProperties)
        if(flags == 3){
            Log.i(tag, "Triggering for triple tap")
            handleTrigger(detectionProperties ?: return, true)
        }
    }

    private fun handleTrigger(detectionProperties: GestureSensor.DetectionProperties, isTripleTap: Boolean) {
        lifecycleScope.launchWhenCreated {
            onTriggered(detectionProperties, isTripleTap)
        }
    }

    final override fun onTrigger(detectionProperties: GestureSensor.DetectionProperties?) {
        handleTrigger(detectionProperties ?: return, false)
    }

    abstract suspend fun onTriggered(detectionProperties: GestureSensor.DetectionProperties, isTripleTap: Boolean)

    open fun onDestroy() {
        //Override if you want to have custom handling for when the lifecycle is destroyed
    }

    @CallSuper
    override fun isAvailable(): Boolean {
        if(whenGates.any { !it.isBlocking }) return false
        return true
    }

    final override fun getLifecycle(): Lifecycle {
        return serviceLifecycle
    }

    protected fun notifyListeners() {
        listeners.forEach { it.onActionAvailabilityChanged(this) }
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

    private fun showAccessibilityNotification() {
        val intent = context.getAccessibilityIntent(TapTapAccessibilityService::class.java)
        showActionNotification(
            intent,
            R.string.notification_action_accessibility_error_title,
            R.string.notification_action_accessibility_error_content
        )
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

    private fun showGestureAccessibilityNotification() {
        val intent = context.getAccessibilityIntent(TapTapAccessibilityService::class.java)
        showActionNotification(
            intent,
            R.string.notification_action_gesture_accessibility_error_title,
            R.string.notification_action_gesture_accessibility_error_content
        )
    }

    protected fun showShizukuNotification(contentRes: Int) {
        val shizukuIntent = context.packageManager.getLaunchIntentForPackage(ShizukuProvider.MANAGER_APPLICATION_ID) ?: return
        showActionNotification(
            shizukuIntent,
            R.string.notification_service_running_error_title,
            contentRes
        )
    }

    protected fun showRootNotification() {
        val mainIntent = context.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID) ?: return
        showActionNotification(
            mainIntent,
            R.string.notification_service_error_root_title,
            R.string.notification_service_error_root_content
        )
    }

    /**
     *  Android 11 introduces restrictions that prevent activity launches from a foreground service,
     *  so we need the draw over other apps permission
     */
    protected fun showOverlayNotificationIfNeeded(): Boolean {
        return if(!Settings.canDrawOverlays(context)) {
            showOverlayNotification()
            true
        }else false
    }

    /**
     *  Shows the accessibility required notification if not enabled, returning true. Otherwise
     *  returns false
     */
    protected fun showAccessibilityNotificationIfNeeded(): Boolean {
        return if(!context.isServiceRunning(TapTapAccessibilityService::class.java)) {
            showAccessibilityNotification()
            true
        }else false
    }

    /**
     *  Shows the gesture accessibility required notification if not enabled, returning true. Otherwise
     *  returns false
     */
    protected fun showGestureAccessibilityNotificationIfNeeded(): Boolean {
        return if(!context.isServiceRunning(TapTapAccessibilityService::class.java)) {
            showGestureAccessibilityNotification()
            true
        }else false
    }

    /**
     *  Returns whether any of the set when gates are passive, ie. the sensor listener should not be
     *  stopped.
     */
    fun passiveWhenGatesSet(): Boolean {
        return whenGates.any { it.isPassive() }
    }

}