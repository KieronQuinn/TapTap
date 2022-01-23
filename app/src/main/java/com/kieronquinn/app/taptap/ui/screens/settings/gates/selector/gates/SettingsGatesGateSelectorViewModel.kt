package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.gate.TapTapGateCategory
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddGenericViewModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SettingsGatesGateSelectorViewModel(navigation: ContainerNavigation, gatesRepository: GatesRepository) : SettingsGatesAddGenericViewModelImpl(navigation, gatesRepository) {

    abstract val state: StateFlow<State>
    abstract val searchShowClear: StateFlow<Boolean>
    abstract val searchText: StateFlow<CharSequence>

    abstract fun setupWithCategory(category: TapTapGateCategory, context: Context)
    abstract fun setSearchText(text: CharSequence)

    sealed class State {
        object Loading : State()
        data class Loaded(val gates: List<TapTapGateDirectory>) : State()
    }

}

class SettingsGatesGateSelectorViewModelImpl(context: Context, private val navigation: ContainerNavigation, gatesRepository: GatesRepository) :
    SettingsGatesGateSelectorViewModel(navigation, gatesRepository) {

    private val gates = MutableSharedFlow<List<TapTapGateDirectory>>()

    private val _searchText = MutableStateFlow("")
    override val searchText = _searchText.asStateFlow()

    override val state = combine(gates, searchText) { a, s ->
        a.filter {
            context.getString(it.nameRes).lowercase().contains(s.lowercase()) ||
                    context.getString(it.descriptionRes).lowercase().contains(s.lowercase())
        }
    }.map { State.Loaded(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val searchShowClear = searchText.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun setupWithCategory(category: TapTapGateCategory, context: Context) {
        if (state.value !is State.Loading) return
        viewModelScope.launch {
            gates.emit(loadGatesForCategory(category, context))
        }
    }

    override fun setSearchText(text: CharSequence) {
        viewModelScope.launch {
            _searchText.emit(text.toString())
        }
    }

    private suspend fun loadGatesForCategory(
        category: TapTapGateCategory,
        context: Context
    ): List<TapTapGateDirectory> = withContext(Dispatchers.IO) {
        return@withContext TapTapGateDirectory.values()
            .filter { it.category == category }
            .sortedBy { context.getString(it.nameRes) }
    }

}