package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.deserialize
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject

class LaunchShortcutAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    private val launchIntentString: String,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    override val tag = "LaunchShortcutAction"

    private val shortcutPackageNames by lazy {
        val launchIntent = Intent().apply {
            deserialize(launchIntentString)
        }
        val launchActivity = context.packageManager.queryIntentActivities(launchIntent, 0)
        if(launchActivity.isEmpty()) return@lazy emptyList()
        launchActivity.map {
            it.activityInfo.packageName
        }.distinct()
    }

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private var isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { shortcutPackageNames.contains((it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName) }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycleScope.launchWhenCreated {
            isOpen.collect {
                notifyListeners()
            }
        }
    }

    override fun isAvailable(): Boolean {
        if(isOpen.value) return false
        return super.isAvailable()
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        if(showOverlayNotificationIfNeeded()) return
        try {
            val launchIntent = Intent().apply {
                deserialize(launchIntentString)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
        }catch (e: Exception){
            e.printStackTrace()
            //Special case for CALL_PHONE required permissions (it'd be a really stupid idea for someone to actually *want* that action though...)
            if(e.message?.contains("android.permission.CALL_PHONE") == true){
                Toast.makeText(context, context.getString(R.string.call_phone_permission_toast), Toast.LENGTH_LONG).show()
            }
        }
    }

}