package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.TapTapActionCategory
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddGenericViewModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SettingsActionsActionSelectorViewModel(navigation: ContainerNavigation, actionsRepository: ActionsRepository) : SettingsActionsAddGenericViewModelImpl(navigation, actionsRepository) {

    abstract val state: StateFlow<State>
    abstract val searchShowClear: StateFlow<Boolean>
    abstract val searchText: StateFlow<CharSequence>

    abstract fun setupWithCategory(category: TapTapActionCategory, context: Context)
    abstract fun setSearchText(text: CharSequence)

    sealed class State {
        object Loading : State()
        data class Loaded(val actions: List<TapTapActionDirectory>) : State()
    }

}

class SettingsActionsActionSelectorViewModelImpl(context: Context, private val navigation: ContainerNavigation, actionsRepository: ActionsRepository) :
    SettingsActionsActionSelectorViewModel(navigation, actionsRepository) {

    private val actions = MutableSharedFlow<List<TapTapActionDirectory>>()

    private val _searchText = MutableStateFlow("")
    override val searchText = _searchText.asStateFlow()

    override val state = combine(actions, searchText) { a, s ->
        a.filter {
            context.getString(it.nameRes).lowercase().contains(s.lowercase()) ||
                    context.getString(it.descriptionRes).lowercase().contains(s.lowercase())
        }
    }.map { State.Loaded(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val searchShowClear = searchText.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun setupWithCategory(category: TapTapActionCategory, context: Context) {
        if (state.value !is State.Loading) return
        viewModelScope.launch {
            actions.emit(loadActionsForCategory(category, context))
        }
    }

    override fun setSearchText(text: CharSequence) {
        viewModelScope.launch {
            _searchText.emit(text.toString())
        }
    }

    private suspend fun loadActionsForCategory(
        category: TapTapActionCategory,
        context: Context
    ): List<TapTapActionDirectory> = withContext(Dispatchers.IO) {
        return@withContext TapTapActionDirectory.values()
            .filter { it.category == category }
            .sortedBy { context.getString(it.nameRes).lowercase() }
    }

}