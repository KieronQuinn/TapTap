package com.kieronquinn.app.taptap.components.columbus.gates

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.columbus.gates.Gate
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.getAccessibilityIntent
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.extensions.runOnDestroy
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import org.koin.core.component.KoinComponent

/**
 *  [Gate] that has [Lifecycle] capabilities, to allow cleaning up when the
 *  lifecycle is destroyed (service dies). All Gates should extend from this.
 */
abstract class TapTapGate (
    private val serviceLifecycle: Lifecycle,
    context: Context,
    notifyHandler: Handler = Handler(Looper.getMainLooper())
): Gate(context, notifyHandler), LifecycleOwner, KoinComponent {

    init {
        serviceLifecycle.runOnDestroy {
            listeners.clear()
            maybeDeactivate()
            onDestroy()
        }
    }

    final override val isBlocking: Boolean
        get() = isBlocked() || super.isBlocking

    abstract fun isBlocked(): Boolean

    override fun onActivate() {
        //No-op by default
    }

    override fun onDeactivate() {
        //No-op by default
    }

    open fun onDestroy() {
        //Override if you want to have custom handling for when the lifecycle is destroyed
    }

    override fun getLifecycle(): Lifecycle {
        return serviceLifecycle
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

    private fun showAccessibilityNotification() {
        val intent = context.getAccessibilityIntent(TapTapAccessibilityService::class.java)
        showGateNotification(
            intent,
            R.string.notification_action_accessibility_error_title,
            R.string.notification_gate_accessibility_error_content
        )
    }

    private fun showGateNotification(clickIntent: Intent, @StringRes titleRes: Int, @StringRes contentRes: Int) {
        TapTapNotificationChannel.Action.showNotification(context, TapTapNotificationId.GATE) {
            val content = context.getString(contentRes)
            it.setOngoing(false)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(context.getString(titleRes))
            it.setContentText(content)
            it.setAutoCancel(true)
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.setContentIntent(PendingIntent.getActivity(context, TapTapNotificationIntentId.GATE_CLICK.ordinal, clickIntent, PendingIntent.FLAG_IMMUTABLE))
            it.priority = NotificationCompat.PRIORITY_HIGH
        }
    }

}

/**
 *  Enables a special case in the service, where stopListening will not be called as a result
 *  of this gate reporting it is blocking, as it is not capable of re-notifying when it is no
 *  longer blocking.
 */
interface PassiveGate