package com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates

import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.models.gate.TapTapUIWhenGate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateDouble
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateTriple
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepository
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsActionsWhenGatesViewModel : ViewModel() {

    abstract val state: StateFlow<State>
    abstract val fabState: StateFlow<FabState>

    abstract fun onAddRequirementFabClicked()
    abstract fun setupWithAction(action: TapTapUIAction, context: Context, forceReload: Boolean = false)
    abstract fun handleGateResult(actionId: Int, gate: TapTapUIWhenGate, context: Context)
    abstract fun removeWhenGate(actionId: Int, id: Int)
    abstract fun onItemSelectionStateChange(selected: Boolean)
    abstract fun onPause()
    abstract fun onResume()

    abstract val scrollToBottomBus: Flow<Unit>
    abstract val reloadServiceBus: Flow<Unit>

    data class SettingsWhenGatesItem(val whenGate: TapTapUIWhenGate, var isSelected: Boolean = false)

    sealed class State {
        object Loading : State()
        data class Loaded(val action: TapTapUIAction, val gates: List<TapTapUIWhenGate>) : State()
    }

    enum class FabState {
        HIDDEN, ADD, DELETE
    }

}

abstract class SettingsActionsWhenGatesViewModelDouble(context: Context, navigation: ContainerNavigation, whenGatesRepository: WhenGatesRepository) : SettingsActionsWhenGatesViewModelBase<WhenGateDouble>(context, navigation, whenGatesRepository)
abstract class SettingsActionsWhenGatesViewModelTriple(context: Context, navigation: ContainerNavigation, whenGatesRepository: WhenGatesRepository) : SettingsActionsWhenGatesViewModelBase<WhenGateTriple>(context, navigation, whenGatesRepository)

abstract class SettingsActionsWhenGatesViewModelBase<T: WhenGate>(context: Context, private val navigation: ContainerNavigation, private val whenGatesRepository: WhenGatesRepository) : SettingsActionsWhenGatesViewModel() {

    private val isResumed = MutableStateFlow(false)
    private val itemSelected = MutableStateFlow(false)
    override val scrollToBottomBus = MutableSharedFlow<Unit>()
    override val reloadServiceBus = whenGatesRepository.onChanged
    
    abstract fun createWhenGate(actionId: Int, newIndex: Int, gate: TapTapUIWhenGate): T

    private val action = MutableStateFlow<TapTapUIAction?>(null)

    override val state = action.filterNotNull().flatMapLatest { uiAction ->
        whenGatesRepository.getWhenGatesAsFlow(uiAction.id).map {
            State.Loaded(uiAction, it.mapNotNull { whenGate ->
                val rawGate = TapTapGateDirectory.valueFor(whenGate.name) ?: return@mapNotNull null
                val gate = TapTapUIGate(
                    whenGate.whenGateId,
                    rawGate,
                    true,
                    whenGate.index,
                    whenGate.extraData,
                    whenGatesRepository.getFormattedDescriptionForWhenGate(context, rawGate, whenGate.extraData)
                )
                TapTapUIWhenGate(whenGate.whenGateId, gate, whenGate.invert)
            })
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val fabState by lazy {
        combine(state, itemSelected, isResumed) { s, i, r ->
            if(!r) return@combine FabState.HIDDEN
            if(s !is State.Loaded) return@combine FabState.HIDDEN
            if(i) FabState.DELETE else FabState.ADD
        }.stateIn(viewModelScope, SharingStarted.Eagerly, FabState.HIDDEN)
    }

    override fun setupWithAction(action: TapTapUIAction, context: Context, forceReload: Boolean) {
        viewModelScope.launch {
            this@SettingsActionsWhenGatesViewModelBase.action.emit(action)
        }
    }

    override fun onResume() {
        viewModelScope.launch {
            isResumed.emit(true)
        }
    }

    override fun onPause() {
        viewModelScope.launch {
            isResumed.emit(false)
        }
    }

    override fun onItemSelectionStateChange(selected: Boolean) {
        viewModelScope.launch {
            itemSelected.emit(selected)
        }
    }

    override fun onAddRequirementFabClicked() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_add_gate, bundleOf("is_requirement" to true))
        }
    }

    override fun handleGateResult(actionId: Int, gate: TapTapUIWhenGate, context: Context) {
        viewModelScope.launch {
            addWhenGate(actionId, gate)
        }
    }

    private suspend fun addWhenGate(actionId: Int, gate: TapTapUIWhenGate) {
        val newIndex = whenGatesRepository.getNextWhenGateIndex(actionId)
        val whenGate = createWhenGate(actionId, newIndex, gate)
        whenGatesRepository.addWhenGate(whenGate)
        scrollToBottomBus.emit(Unit)
    }

    override fun removeWhenGate(actionId: Int, id: Int) {
        viewModelScope.launch {
            whenGatesRepository.removeWhenGate(actionId, id)
        }
    }

}

class SettingsActionsWhenGatesViewModelDoubleImpl(
    context: Context, 
    navigation: ContainerNavigation, 
    whenGatesRepository: WhenGatesRepositoryDouble<*>
): SettingsActionsWhenGatesViewModelDouble(context, navigation, whenGatesRepository) {

    override fun createWhenGate(actionId: Int, newIndex: Int, gate: TapTapUIWhenGate): WhenGateDouble {
        return WhenGateDouble(
            actionId = actionId,
            name = gate.gate.gate.name,
            invert = gate.inverted,
            index = newIndex,
            extraData = gate.gate.extraData
        )
    }
    
}

class SettingsActionsWhenGatesViewModelTripleImpl(
    context: Context,
    navigation: ContainerNavigation,
    whenGatesRepository: WhenGatesRepositoryTriple<*>
): SettingsActionsWhenGatesViewModelTriple(context, navigation, whenGatesRepository) {

    override fun createWhenGate(actionId: Int, newIndex: Int, gate: TapTapUIWhenGate): WhenGateTriple {
        return WhenGateTriple(
            actionId = actionId,
            name = gate.gate.gate.name,
            invert = gate.inverted,
            index = newIndex,
            extraData = gate.gate.extraData
        )
    }

}