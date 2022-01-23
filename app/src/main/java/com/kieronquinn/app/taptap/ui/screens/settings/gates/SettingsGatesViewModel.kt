package com.kieronquinn.app.taptap.ui.screens.settings.gates

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

abstract class SettingsGatesViewModel : GenericSettingsViewModel() {

    abstract fun reloadGates()
    abstract suspend fun getGates(): List<Gate>

    abstract fun moveGate(fromIndex: Int, toIndex: Int)
    abstract fun removeGate(id: Int)
    abstract fun onAddGateFabClicked()

    abstract fun onGateResult(gate: TapTapUIGate)
    abstract fun onItemSelectionStateChange(selected: Boolean)
    abstract fun onItemStateChanged(id: Int, enabled: Boolean)
    abstract fun onPause()
    abstract fun onResume()

    abstract val state: StateFlow<State>
    abstract val fabState: StateFlow<FabState>
    abstract val scrollToBottomBus: Flow<Unit>
    abstract val reloadServiceBus: Flow<Unit>

    abstract val header: SettingsGatesItem?
    abstract val showHeader: StateFlow<Boolean>

    sealed class State {
        object Loading : State()
        data class Loaded(val items: ArrayList<SettingsGatesItem>) : State()
    }

    enum class FabState {
        HIDDEN, ADD, DELETE
    }

    sealed class SettingsGatesItem(val type: SettingsGatesItemType) {

        data class Gate(val gate: TapTapUIGate, var isSelected: Boolean = false) :
            SettingsGatesItem(SettingsGatesItemType.GATE)

        data class Header(@StringRes val contentRes: Int, val onCloseClick: () -> Unit, val onClick: (() -> Unit)? = null):
            SettingsGatesItem(SettingsGatesItemType.HEADER)

        enum class SettingsGatesItemType {
            GATE, HEADER
        }
    }

}

class SettingsGatesViewModelImpl(
    context: Context,
    private val gatesRepository: GatesRepository,
    private val navigation: ContainerNavigation,
    private val settings: TapTapSettings
) : SettingsGatesViewModel() {

    private val isResumed = MutableStateFlow(false)
    private val reloadBus = MutableSharedFlow<Unit>()
    private val itemSelected = MutableStateFlow(false)
    override val scrollToBottomBus = MutableSharedFlow<Unit>()
    override val reloadServiceBus = gatesRepository.onChanged
    private val showHelp = settings.gatesShowHelp
    override val showHeader = showHelp.asFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, showHelp.getSync())

    override val header = SettingsGatesItem.Header(R.string.help_gate, ::onHeaderDismissClicked, ::onHeaderClicked)

    private val gates = flow {
        emit(loadGates(context).map {
            SettingsGatesItem.Gate(it)
        }.toTypedArray())
    }

    override val state = combine(gates, showHeader, reloadBus) { g, s, _ ->
        val items = listOfNotNull(if(s && g.isNotEmpty()) header else null, *g)
        State.Loaded(ArrayList(items))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val fabState by lazy {
        combine(state, itemSelected, isResumed) { s, i, r ->
            if (!r) return@combine FabState.HIDDEN
            if (s !is State.Loaded) return@combine FabState.HIDDEN
            if (i) FabState.DELETE else FabState.ADD
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            FabState.HIDDEN
        )
    }

    override fun reloadGates() {
        if (state.value !is State.Loading) return
        viewModelScope.launch {
            reloadBus.emit(Unit)
        }
    }

    override fun onAddGateFabClicked() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_add_gate)
        }
    }

    private suspend fun loadGates(context: Context): List<TapTapUIGate> = withContext(
        Dispatchers.IO
    ) {
        val gates = getGates().mapNotNull { gate ->
            val item = TapTapGateDirectory.valueFor(gate.name) ?: return@mapNotNull null
            TapTapUIGate(
                gate.gateId,
                item,
                gate.enabled,
                gate.index,
                gate.extraData,
                getFormattedDescriptionForGate(context, item, gate.extraData)
            )
        }.sortedBy { it.index }
        return@withContext gates
    }

    override suspend fun getGates() = gatesRepository.getSavedGates()

    private suspend fun addGateToRepo(gate: Gate): Long {
        return gatesRepository.addGate(gate)
    }

    private fun removeGateInternal(id: Int) {
        viewModelScope.launch {
            gatesRepository.removeGate(id)
        }
    }

    override fun onGateResult(gate: TapTapUIGate) {
        viewModelScope.launch {
            handleGateResult(gate)
            //Allow UI to catch up
            delay(500)
            scrollToBottomBus.emit(Unit)
        }
    }

    private suspend fun handleGateResult(gate: TapTapUIGate) {
        val newIndex = gatesRepository.getNextGateIndex()
        val repoGate = createGate(gate, newIndex)
        val id = addGateToRepo(repoGate).toInt()
        gate.id = id
    }

    override fun moveGate(fromIndex: Int, toIndex: Int) {
        if(showHeader.value){
            //Adjust for header
            moveGateInternal(fromIndex - 1, toIndex - 1)
        }else{
            moveGateInternal(fromIndex, toIndex)
        }
    }

    private fun moveGateInternal(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            gatesRepository.moveGate(fromIndex, toIndex)
        }
    }

    override fun onItemSelectionStateChange(selected: Boolean) {
        viewModelScope.launch {
            itemSelected.emit(selected)
        }
    }

    override fun removeGate(id: Int) {
        viewModelScope.launch {
            removeGateInternal(id)
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

    override fun onItemStateChanged(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            gatesRepository.setGateEnabled(id, enabled)
        }
    }

    private fun getFormattedDescriptionForGate(
        context: Context,
        gate: TapTapGateDirectory,
        data: String?
    ): CharSequence {
        return gatesRepository.getFormattedDescriptionForGate(context, gate, data)
    }

    private fun createGate(uiGate: TapTapUIGate, index: Int): Gate {
        return Gate(
            name = uiGate.gate.name,
            enabled = uiGate.enabled,
            index = index,
            extraData = uiGate.extraData
        )
    }

    private fun onHeaderDismissClicked() {
        viewModelScope.launch {
            showHelp.set(false)
        }
    }

    private fun onHeaderClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsGatesFragmentDirections.actionSettingsGatesFragmentToSettingsGatesHelpBottomSheetFragment())
        }
    }

}