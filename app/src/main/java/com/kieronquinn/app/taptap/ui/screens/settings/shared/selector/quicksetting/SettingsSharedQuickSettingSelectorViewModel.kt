package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository.QuickSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsSharedQuickSettingSelectorViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onQuickSettingClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(val tiles: List<QuickSetting>): State()
    }

}

class SettingsSharedQuickSettingSelectorViewModelImpl(quickSettingsRepository: QuickSettingsRepository, private val navigation: ContainerNavigation): SettingsSharedQuickSettingSelectorViewModel() {

    override val state = flow {
        emit(State.Loaded(quickSettingsRepository.getQuickSettings()))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onQuickSettingClicked() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_shared_picker_quick_setting, true)
        }
    }

}