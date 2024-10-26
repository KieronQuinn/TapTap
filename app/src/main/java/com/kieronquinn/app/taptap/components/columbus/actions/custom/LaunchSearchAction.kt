package com.kieronquinn.app.taptap.components.columbus.actions.custom

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.columbus.feedback.FeedbackEffect
import com.google.android.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.utils.extensions.isPackageAssistant
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.inject

class LaunchSearchAction(
    serviceLifecycle: Lifecycle,
    private val context: Context,
    whenGates: List<TapTapWhenGate>,
    effects: Set<FeedbackEffect>
) : TapTapAction(
    serviceLifecycle, context, whenGates, effects, requiresUnlock = true
) {

    override val tag = "LaunchSearchAction"

    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private var isOpen = accessibilityRouter.accessibilityOutputBus.filter { it is TapTapAccessibilityRouter.AccessibilityOutput.AppOpen }
        .map { context.isPackageAssistant((it as TapTapAccessibilityRouter.AccessibilityOutput.AppOpen).packageName) }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, false)

    init {
        lifecycle.whenCreated {
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

        val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val globalSearchActivity = searchManager.globalSearchActivity ?: return

        val intent = Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            component = globalSearchActivity

            putExtra(SearchManager.QUERY, "")
            putExtra(SearchManager.EXTRA_SELECT_QUERY, true)

            val appSearchData = Bundle()
            appSearchData.putString("source", context.packageName)

            putExtra(SearchManager.APP_DATA, appSearchData)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

}