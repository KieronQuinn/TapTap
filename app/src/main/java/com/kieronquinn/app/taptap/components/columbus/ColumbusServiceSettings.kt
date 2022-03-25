package com.kieronquinn.app.taptap.components.columbus

import androidx.lifecycle.LifecycleOwner
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.feedback.TapTapFeedbackEffect
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.components.settings.TapModel
import org.koin.core.component.KoinComponent

/**
 *  Holds the settings for the Columbus service to use when initializing
 */
data class ColumbusServiceSettings (
    private var _lifecycleOwner: LifecycleOwner? = null,
    private var _actions: List<TapTapAction>? = null,
    private var _tripleTapActions: List<TapTapAction>? = null,
    private var _gates: List<TapTapGate>? = null,
    private var _feedbackEffects: List<TapTapFeedbackEffect>? = null,
    private var _isTripleTapEnabled: Boolean? = null,
    private var _useContextHub: Boolean? = null,
    private var _useContextHubLogging: Boolean? = null,
    private var _tapModel: TapModel? = null
): KoinComponent {
    
    val lifecycleOwner
        get() = _lifecycleOwner ?: throw NullPointerException("LifecycleOwner not set in ColumbusServiceSettings")

    val actions
        get() = _actions ?: throw NullPointerException("Actions not set in ColumbusServiceSettings")

    val tripleTapActions
        get() = _tripleTapActions ?: throw NullPointerException("Triple Tap Actions not set in ColumbusServiceSettings")

    val gates
        get() = _gates ?: throw NullPointerException("Gates not set in ColumbusServiceSettings")

    val feedbackEffects
        get() = _feedbackEffects ?: throw NullPointerException("Feedback effects not set in ColumbusServiceSettings")

    val isTripleTapEnabled
        get() = _isTripleTapEnabled ?: throw NullPointerException("isTripleTapEnabled not set in ColumbusServiceSettings")

    val useContextHub
        get() = _useContextHub ?: throw NullPointerException("useContextHub not set in ColumbusServiceSettings")

    val useContextHubLogging
        get() = _useContextHubLogging ?: throw NullPointerException("useContextHubLogging not set in ColumbusServiceSettings")

    val tapModel
        get() = _tapModel ?: throw NullPointerException("tapModel not set in ColumbusServiceSettings")

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner){
        this._lifecycleOwner = lifecycleOwner
    }

    fun setActions(actions: List<TapTapAction>) {
        this._actions = actions
    }

    fun setTripleTapActions(actions: List<TapTapAction>) {
        this._tripleTapActions = actions
    }

    fun setGates(gates: List<TapTapGate>) {
        this._gates = gates
    }

    fun setFeedbackEffects(effects: List<TapTapFeedbackEffect>) {
        this._feedbackEffects = effects
    }

    fun setTripleTapEnabled(enabled: Boolean) {
        this._isTripleTapEnabled = enabled
    }

    fun setUseContextHub(enabled: Boolean) {
        this._useContextHub = enabled
    }

    fun setUseContextHubLogging(enabled: Boolean) {
        this._useContextHubLogging = enabled
    }

    fun setTapModel(tapModel: TapModel) {
        this._tapModel = tapModel
    }

}