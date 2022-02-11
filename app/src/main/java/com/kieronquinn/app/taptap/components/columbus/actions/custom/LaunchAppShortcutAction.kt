package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.models.columbus.AppShortcutData
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.parcelize.Parcelize
import org.koin.core.component.inject

class LaunchAppShortcutAction(
    serviceLifecycle: Lifecycle,
    context: Context,
    extraData: String,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>,
    private val service: TapTapShizukuServiceRepository
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects
) {

    private val gson by inject<Gson>()

    private val appShortcutData = gson.fromJson(extraData, AppShortcutData::class.java)

    override val tag = "AppShortcutAction"

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private var isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { (it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName == appShortcutData.packageName }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycleScope.launchWhenCreated {
            isOpen.collect {
                notifyListeners()
            }
        }
    }

    override suspend fun onTriggered(
        detectionProperties: GestureSensor.DetectionProperties,
        isTripleTap: Boolean
    ) {
        val result = service.runWithShellService {
            it.startShortcut(appShortcutData.packageName, appShortcutData.shortcutId)
        }
        if(result is TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed){
            showShizukuNotification(result.reason.contentRes)
        }
    }

    override fun isAvailable(): Boolean {
        if(isOpen.value) return false
        return super.isAvailable()
    }

}