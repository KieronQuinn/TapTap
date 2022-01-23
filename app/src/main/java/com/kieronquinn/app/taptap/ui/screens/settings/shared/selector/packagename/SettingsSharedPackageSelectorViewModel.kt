package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsSharedPackageSelectorViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract val searchText: StateFlow<String>
    abstract val searchShowClear: StateFlow<Boolean>
    abstract fun setShowAllApps(showAllApps: Boolean)
    abstract fun setSearchText(text: CharSequence)
    abstract fun onAppClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<App>): State()
    }

    data class App(val name: CharSequence, val packageName: String, val launchable: Boolean)

}

class SettingsSharedPackageSelectorViewModelImpl(context: Context, private val navigation: ContainerNavigation): SettingsSharedPackageSelectorViewModel() {

    private val packageManager = context.packageManager

    private val apps = flow {
        val apps = packageManager.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS).map {
            App(it.loadLabel(packageManager), it.packageName, packageManager.getLaunchIntentForPackage(it.packageName) != null)
        }.sortedBy { it.name.toString() }
        emit(apps)
    }.flowOn(Dispatchers.IO)

    private val showAllApps = MutableStateFlow(false)
    override val searchText = MutableStateFlow("")
    override val searchShowClear = searchText.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    override val state = combine(apps, searchText, showAllApps) { a, s, sa ->
        State.Loaded(a.filter {
            it.name.toString().trim().lowercase().contains(s.trim().lowercase()) && (sa || it.launchable)
        })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onAppClicked() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_shared_picker_package, true)
        }
    }

    override fun setSearchText(text: CharSequence) {
        viewModelScope.launch {
            searchText.emit(text.toString())
        }
    }

    override fun setShowAllApps(showAllApps: Boolean) {
        viewModelScope.launch {
            this@SettingsSharedPackageSelectorViewModelImpl.showAllApps.emit(showAllApps)
        }
    }

}