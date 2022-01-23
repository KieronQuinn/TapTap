package com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.utils.extensions.Shizuku_requestPermissionIfNeeded
import com.kieronquinn.app.taptap.utils.extensions.getPlayStoreIntentForPackage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rikka.shizuku.ShizukuProvider

abstract class SettingsSharedShizukuPermissionFlowViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun popBackstack()
    abstract fun onShizukuClicked(context: Context)
    abstract fun onSuiClicked(context: Context)

    sealed class State {
        object Loading: State()
        object PermissionGranted: State()
        object NoShizuku: State()
    }

}

class SettingsSharedShizukuPermissionFlowViewModelImpl(private val navigation: ContainerNavigation): SettingsSharedShizukuPermissionFlowViewModel() {

    companion object {
        //Minimum time required for the previous fragment to be destroyed and be ready to return to
        private const val MIN_TIME = 500L
    }

    override val state = flow {
        val startTime = System.currentTimeMillis()
        val permissionGranted = Shizuku_requestPermissionIfNeeded() ?: false
        val timeRemaining = MIN_TIME - (System.currentTimeMillis() - startTime)
        if(timeRemaining > 0){
            delay(timeRemaining)
        }
        if(permissionGranted){
            emit(State.PermissionGranted)
        }else{
            emit(State.NoShizuku)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun popBackstack() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_shared_shizuku_permission_flow, true)
        }
    }

    override fun onShizukuClicked(context: Context) {
        viewModelScope.launch {
            val shizukuIntent = context.getPlayStoreIntentForPackage(ShizukuProvider.MANAGER_APPLICATION_ID, "https://shizuku.rikka.app/download/")
            navigation.navigate(shizukuIntent ?: return@launch)
        }
    }

    override fun onSuiClicked(context: Context) {
        viewModelScope.launch {
            val suiIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/RikkaApps/Sui")
            }
            navigation.navigate(suiIntent)
        }
    }

}