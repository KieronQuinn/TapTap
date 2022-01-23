package com.kieronquinn.app.taptap.ui.screens.container

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.SETTINGS_VERSION
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.update.UpdateRepository
import com.kieronquinn.app.taptap.ui.screens.settings.main.SettingsMainFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.settings.more.SettingsMoreFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.settings.options.SettingsOptionsFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.settings.update.SettingsUpdateFragmentDirections
import com.kieronquinn.app.taptap.utils.extensions.getColumbusSettingAsFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class ContainerViewModel: ViewModel() {

    abstract val showUpdateSnackbar: StateFlow<Boolean>
    abstract val columbusSettingPhoenixBus: Flow<Unit>

    abstract fun writeSettingsVersion()
    abstract fun setCanShowSnackbar(showSnackbar: Boolean)
    abstract fun onNavigationItemClicked(id: Int)
    abstract fun onBackPressed()
    abstract fun onUpdateClicked()
    abstract fun onUpdateDismissed()
    abstract fun phoenix()

}

class ContainerViewModelImpl(context: Context, private val settings: TapTapSettings, private val navigation: ContainerNavigation, updateRepository: UpdateRepository): ContainerViewModel() {

    private val canShowSnackbar = MutableStateFlow(false)
    private val hasDismissedSnackbar = MutableStateFlow(false)

    private val gitHubUpdate = flow {
        emit(updateRepository.getUpdate())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val showUpdateSnackbar = combine(canShowSnackbar, gitHubUpdate, hasDismissedSnackbar){ canShow, update, dismissed ->
        canShow && update != null && !dismissed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val columbusSettingPhoenixBus = context.getColumbusSettingAsFlow().drop(1).map {  }

    override fun onNavigationItemClicked(id: Int) {
        viewModelScope.launch {
            when(id){
                R.id.menu_bottom_nav_main_gesture -> navigation.navigate(SettingsMainFragmentDirections.actionGlobalSettingsMainFragment())
                R.id.menu_bottom_nav_main_settings -> navigation.navigate(SettingsOptionsFragmentDirections.actionGlobalSettingsOptionsFragment())
                R.id.menu_bottom_nav_main_more -> navigation.navigate(SettingsMoreFragmentDirections.actionGlobalSettingsMoreFragment())
            }
        }
    }

    override fun onBackPressed() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun setCanShowSnackbar(showSnackbar: Boolean) {
        viewModelScope.launch {
            canShowSnackbar.emit(showSnackbar)
        }
    }

    override fun onUpdateClicked() {
        val release = gitHubUpdate.value ?: return
        viewModelScope.launch {
            navigation.navigate(SettingsUpdateFragmentDirections.actionGlobalSettingsUpdateFragment(release))
        }
    }

    override fun onUpdateDismissed() {
        viewModelScope.launch {
            hasDismissedSnackbar.emit(true)
        }
    }

    override fun phoenix() {
        viewModelScope.launch {
            navigation.phoenix()
        }
    }

    override fun writeSettingsVersion() {
        viewModelScope.launch {
            settings.settingsVersion.set(SETTINGS_VERSION)
        }
    }

}