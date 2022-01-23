package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.TapTapActionCategory
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsActionsAddCategorySelectorViewModel(navigation: ContainerNavigation, actionsRepository: ActionsRepository): SettingsActionsAddGenericViewModelImpl(navigation, actionsRepository) {

    abstract val state: StateFlow<State>
    abstract val searchShowClear: StateFlow<Boolean>
    abstract val searchText: StateFlow<CharSequence>

    abstract fun onCategoryClicked(category: TapTapActionCategory)
    abstract fun setSearchText(text: CharSequence)

    sealed class State {
        object Loading : State()
        data class CategoryPicker(val categories: List<TapTapActionCategory>) : State()
        data class ItemPicker(val items: List<TapTapActionDirectory>): State()
    }

}

class SettingsActionsAddCategorySelectorViewModelImpl(private val navigation: ContainerNavigation, context: Context, actionsRepository: ActionsRepository) :
    SettingsActionsAddCategorySelectorViewModel(navigation, actionsRepository) {

    private val categories by lazy {
        TapTapActionDirectory.values().associateBy { it.category }.keys.toList()
    }

    private val actions by lazy {
        TapTapActionDirectory.values().sortedBy { context.getString(it.nameRes).lowercase() }
    }

    override val searchText = MutableStateFlow("")

    override val state = searchText.map { s ->
        if(s.isNotBlank()){
            State.ItemPicker(actions.filter { context.getString(it.nameRes).lowercase().contains(s.lowercase()) || context.getString(it.descriptionRes).lowercase().contains(s.lowercase()) })
        }else{
            State.CategoryPicker(categories)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val searchShowClear = searchText.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun setSearchText(text: CharSequence) {
        viewModelScope.launch {
            searchText.emit(text.toString())
        }
    }

    override fun onCategoryClicked(category: TapTapActionCategory) {
        viewModelScope.launch {
            navigation.navigate(
                SettingsActionsAddCategorySelectorFragmentDirections
                    .actionSettingsActionsAddCategorySelectorFragmentToSettingsActionsActionSelectorFragment(category)
            )
        }
    }

}

