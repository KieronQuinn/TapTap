package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.activities.UnlockDeviceActivity
import com.kieronquinn.app.taptap.ui.activities.WakeUpActivity
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class WakeDeviceAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    companion object {
        private const val WAKELOCK_TAG = "TapTap:WakeDeviceAction"
    }

    override val tag = "WakeDeviceAction"

    private val settings by inject<TapTapSettings>()

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private var screenState = context.broadcastReceiverAsFlow(Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT)
        .map {
            !powerManager.isInteractive
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, !powerManager.isInteractive)

    init {
        lifecycleScope.launchWhenCreated {
            screenState.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        return screenState.value && super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        if(settings.advancedLegacyWake.get()) {
            context.startActivity(Intent(context, WakeUpActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(WakeUpActivity.EXTRA_UNLOCK, true)
            })
        }else{
            lifecycleScope.launch {
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    WAKELOCK_TAG
                )
                wakeLock.acquire(5000)
                delay(5000)
                wakeLock.release()
            }
            context.startActivity(Intent(context, UnlockDeviceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

}