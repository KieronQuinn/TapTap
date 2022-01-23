package com.kieronquinn.app.taptap.ui.screens.settings.actions

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.models.gate.TapTapUIWhenGate
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.Action
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGate
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepository
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

abstract class SettingsActionsGenericViewModel<A : Action> : GenericSettingsViewModel() {

    abstract fun reloadActions()
    abstract suspend fun getActions(): List<A>
    abstract suspend fun getWhenGates(): List<WhenGate>

    abstract suspend fun getNextActionIndex(): Int

    abstract fun createAction(uiAction: TapTapUIAction, index: Int): A
    abstract suspend fun addActionToRepo(action: A): Long
    abstract fun moveAction(fromIndex: Int, toIndex: Int)
    protected abstract fun moveActionInternal(fromIndex: Int, toIndex: Int)
    abstract fun removeAction(id: Int)
    abstract fun onAddActionFabClicked()
    abstract fun onWhenGateChipClicked(action: TapTapUIAction)
    abstract fun getFormattedDescriptionForAction(context: Context, action: TapTapActionDirectory, data: String?): CharSequence
    abstract fun onActionResult(action: TapTapUIAction)
    abstract fun onItemSelectionStateChange(selected: Boolean)
    abstract fun onPause()
    abstract fun onResume()

    abstract val state: StateFlow<State>
    abstract val fabState: StateFlow<FabState>
    abstract val scrollToBottomBus: Flow<Unit>
    abstract val reloadServiceBus: Flow<Unit>
    abstract val switchChanged: Flow<Unit>?
    abstract val header: SettingsActionsItem?
    abstract val showHeader: StateFlow<Boolean>

    sealed class State {
        object Loading : State()
        data class Loaded(val items: ArrayList<SettingsActionsItem>) : State()
    }

    enum class FabState {
        HIDDEN, ADD, DELETE
    }

    sealed class SettingsActionsItem(val type: SettingsActionsItemType) {

        data class Action(val action: TapTapUIAction, var isSelected: Boolean = false) :
            SettingsActionsItem(SettingsActionsItemType.ACTION)

        data class Header(@StringRes val contentRes: Int, val onCloseClick: () -> Unit, val onClick: (() -> Unit)? = null):
            SettingsActionsItem(SettingsActionsItemType.HEADER)

        enum class SettingsActionsItemType {
            ACTION, HEADER
        }
    }

}

abstract class SettingsActionsGenericViewModelImpl<T : Action>(context: Context, private val whenGatesRepository: WhenGatesRepository, private val actionsRepository: ActionsRepository, private val gatesRepository: GatesRepository, private val navigation: ContainerNavigation) :
    SettingsActionsGenericViewModel<T>() {

    private val isResumed = MutableStateFlow(false)
    private val reloadBus = MutableSharedFlow<Unit>()
    private val itemSelected = MutableStateFlow(false)
    override val scrollToBottomBus = MutableSharedFlow<Unit>()
    override val reloadServiceBus = actionsRepository.onChanged.debounce(500L)

    private val actions = flow {
        emit(loadActions(context).map { SettingsActionsItem.Action(it) }.toTypedArray())
    }

    override val state by lazy {
        combine(actions, showHeader, reloadBus) { a, s, _ ->
            val items = listOfNotNull(if(s && a.isNotEmpty()) header else null, *a)
            State.Loaded(ArrayList(items))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)
    }

    override val fabState by lazy {
        combine(state, itemSelected, isResumed) { s, i, r ->
            if(!r) return@combine FabState.HIDDEN
            if(s !is State.Loaded) return@combine FabState.HIDDEN
            if(i) FabState.DELETE else FabState.ADD
        }.stateIn(viewModelScope, SharingStarted.Eagerly, FabState.HIDDEN)
    }

    override fun reloadActions() {
        if(state.value !is State.Loading) return
        viewModelScope.launch {
            reloadBus.emit(Unit)
        }
    }

    override fun onAddActionFabClicked() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_add_action)
        }
    }

    private suspend fun loadActions(context: Context): List<TapTapUIAction> = withContext(Dispatchers.IO) {
        val actions = getActions()
        val groupedActions = actions.mapNotNull { action ->
            val rawWhenGates = whenGatesRepository.getSavedWhenGates(action.actionId)
            val gates = rawWhenGates.map { whenGate ->
                val rawGate = TapTapGateDirectory.valueFor(whenGate.name) ?: return@mapNotNull null
                val gate = TapTapUIGate(
                    whenGate.whenGateId,
                    rawGate,
                    true,
                    whenGate.index,
                    whenGate.extraData,
                    gatesRepository.getFormattedDescriptionForGate(context, rawGate, whenGate.extraData)
                )
                TapTapUIWhenGate(whenGate.whenGateId, gate, whenGate.invert)
            }
            val item = TapTapActionDirectory.valueFor(action.name) ?: return@mapNotNull null
            TapTapUIAction(item, action.actionId, action.index, action.extraData, getFormattedDescriptionForAction(context, item, action.extraData), gates.size)
        }
        return@withContext groupedActions
    }

    override fun onActionResult(action: TapTapUIAction) {
        viewModelScope.launch {
            handleActionResult(action)
            //Allow UI to catch up
            delay(500)
            scrollToBottomBus.emit(Unit)
        }
    }

    override fun moveAction(fromIndex: Int, toIndex: Int) {
        //Adjust the indexes to cope with the header if needed
        if(showHeader.value){
            moveActionInternal(fromIndex - 1, toIndex - 1)
        }else{
            moveActionInternal(fromIndex, toIndex)
        }
    }

    private suspend fun handleActionResult(action: TapTapUIAction) {
        val newIndex = getNextActionIndex()
        val repoAction = createAction(action, newIndex)
        addActionToRepo(repoAction).toInt()
        action.id = repoAction.actionId
    }

    override fun onItemSelectionStateChange(selected: Boolean) {
        viewModelScope.launch {
            itemSelected.emit(selected)
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

    override fun getFormattedDescriptionForAction(
        context: Context,
        action: TapTapActionDirectory,
        data: String?
    ): CharSequence {
        return actionsRepository.getFormattedDescriptionForAction(context, action, data)
    }

}