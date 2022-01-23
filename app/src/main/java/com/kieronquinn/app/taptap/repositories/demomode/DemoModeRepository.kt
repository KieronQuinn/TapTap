package com.kieronquinn.app.taptap.repositories.demomode

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.actions.custom.DemoModeAction
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.columbus.feedback.custom.DemoModeFeedback
import com.kieronquinn.app.taptap.components.columbus.feedback.custom.HapticClickFeedback
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface DemoModeRepository {

    val tapBus: Flow<Unit>
    val doubleTapBus: Flow<Unit>
    val tripleTapBus: Flow<Unit>

    fun isDemoModeEnabled(): Boolean
    fun setDemoModeEnabled(enabled: Boolean)
    fun getActions(context: Context, lifecycle: Lifecycle): List<TapTapAction>
    fun getTripleActions(context: Context, lifecycle: Lifecycle): List<TapTapAction>
    fun getGates(context: Context, lifecycle: Lifecycle): List<TapTapGate>
    fun getFeedbackEffects(context: Context, lifecycle: Lifecycle): List<TapTapFeedbackEffect>
    fun getTripleTapEnabled(): Boolean
    fun getUseContextHub(): Boolean

    suspend fun onTapDetected()
    suspend fun onDoubleTapDetected()
    suspend fun onTripleTapDetected()

}

class DemoModeRepositoryImpl: DemoModeRepository {

    override val tapBus = MutableSharedFlow<Unit>()
    override val doubleTapBus = MutableSharedFlow<Unit>()
    override val tripleTapBus = MutableSharedFlow<Unit>()

    private var demoModeEnabled = false

    override fun isDemoModeEnabled() = demoModeEnabled

    override fun setDemoModeEnabled(enabled: Boolean) {
        demoModeEnabled = enabled
    }

    override fun getActions(context: Context, lifecycle: Lifecycle): List<TapTapAction> {
        return listOf(DemoModeAction(lifecycle, context, emptyList(), emptySet()))
    }

    override fun getTripleActions(context: Context, lifecycle: Lifecycle): List<TapTapAction> {
        return listOf(DemoModeAction(lifecycle, context, emptyList(), emptySet()))
    }

    override fun getGates(context: Context, lifecycle: Lifecycle) = emptyList<TapTapGate>()

    override fun getFeedbackEffects(context: Context, lifecycle: Lifecycle): List<TapTapFeedbackEffect> {
        return listOf(HapticClickFeedback(lifecycle, context), DemoModeFeedback(lifecycle))
    }

    //Triple Tap is always enabled during setup
    override fun getTripleTapEnabled() = true

    //Context Hub is always disabled during setup
    override fun getUseContextHub() = false

    override suspend fun onTapDetected() {
        tapBus.emit(Unit)
    }

    override suspend fun onDoubleTapDetected() {
        doubleTapBus.emit(Unit)
    }

    override suspend fun onTripleTapDetected() {
        tripleTapBus.emit(Unit)
    }

}