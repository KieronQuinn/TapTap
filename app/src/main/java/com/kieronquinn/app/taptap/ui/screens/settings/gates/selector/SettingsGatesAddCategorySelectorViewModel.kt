package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.gate.TapTapGateCategory
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsGatesAddCategorySelectorViewModel(navigation: ContainerNavigation, gatesRepository: GatesRepository): SettingsGatesAddGenericViewModelImpl(navigation, gatesRepository) {

    abstract val state: StateFlow<State>
    abstract val searchShowClear: StateFlow<Boolean>
    abstract val searchText: StateFlow<CharSequence>

    abstract fun onCategoryClicked(category: TapTapGateCategory, isRequirement: Boolean)
    abstract fun setSearchText(text: CharSequence)

    sealed class State {
        object Loading : State()
        data class CategoryPicker(val categories: List<TapTapGateCategory>) : State()
        data class ItemPicker(val items: List<TapTapGateDirectory>): State()
    }

}

class SettingsGatesAddCategorySelectorViewModelImpl(private val navigation: ContainerNavigation, context: Context, gatesRepository: GatesRepository) :
    SettingsGatesAddCategorySelectorViewModel(navigation, gatesRepository) {

    private val categories by lazy {
        TapTapGateDirectory.values().associateBy { it.category }.keys.toList()
    }

    private val actions by lazy {
        TapTapGateDirectory.values().sortedBy { context.getString(it.nameRes).lowercase() }
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

    override fun onCategoryClicked(category: TapTapGateCategory, isRequirement: Boolean) {
        viewModelScope.launch {
            navigation.navigate(
                SettingsGatesAddCategorySelectorFragmentDirections
                    .actionSettingsGatesAddCategorySelectorFragmentToSettingsGatesGateSelectorFragment(category, isRequirement)
            )
        }
    }

}