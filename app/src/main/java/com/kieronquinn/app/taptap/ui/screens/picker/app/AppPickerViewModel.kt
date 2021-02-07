package com.kieronquinn.app.taptap.ui.screens.picker.app

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppPickerViewModel: BaseViewModel() {

    val showAllApps = MutableStateFlow(false)
    private val searchTerm = MutableStateFlow("")
    private val apps = MutableStateFlow<List<App>?>(null)

    fun getApps(context: Context) = viewModelScope.launch {
        val packageManager = context.packageManager
        withContext(Dispatchers.IO) {
            val allApps = packageManager.run {
                getInstalledApplications(0).map {
                    App(
                        it.packageName,
                        it.loadLabel(this),
                        getLaunchIntentForPackage(it.packageName) != null
                    )
                }.sortedBy { it.label.toString().toLowerCase(Locale.getDefault()) }
            }
            launch {
                searchTerm.collect {
                    apps.filterAndEmit(allApps)
                }
            }
            launch {
                showAllApps.collect {
                    apps.filterAndEmit(allApps)
                }
            }
        }
    }

    val state by lazy {
        MediatorLiveData<State>().apply {
            addSource(apps.asLiveData()) {
                update(when {
                    it == null -> State.Loading
                    it.isEmpty() -> State.Empty
                    else -> State.Loaded(it)
                })
            }
        }
    }

    val shouldShowClearButton by lazy {
        MediatorLiveData<Boolean>().apply {
            update(false)
            addSource(searchTerm.asLiveData()){
                update(it.isNotEmpty())
            }
        }
    }

    private suspend fun FlowCollector<List<App>>.filterAndEmit(allApps: List<App>){
        val shouldShowAllApps = showAllApps.value
        val currentSearchTerm = searchTerm.value
        emit(allApps.filter { if(shouldShowAllApps) true else it.isLaunchable && if(currentSearchTerm.isNotEmpty())
            it.label.toString().toLowerCase(Locale.getDefault()).contains(currentSearchTerm) else true })
    }

    fun toggleShowAllApps() = viewModelScope.launch {
        showAllApps.emit(!showAllApps.value)
    }

    fun setSearchTerm(searchTerm: String) = viewModelScope.launch {
        this@AppPickerViewModel.searchTerm.emit(searchTerm)
    }

    data class App(val packageName: String, val label: CharSequence, val isLaunchable: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val apps: List<App>): State()
        object Empty: State()
    }

}