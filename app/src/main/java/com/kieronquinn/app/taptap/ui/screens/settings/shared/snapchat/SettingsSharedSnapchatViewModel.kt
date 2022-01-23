package com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.actions.custom.SnapchatAction
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.repositories.snapchat.SnapchatRepository
import com.kieronquinn.app.taptap.utils.extensions.isNetworkConnected
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsSharedSnapchatViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun onResume()
    abstract fun popBackstack(delay: Boolean = false)
    abstract fun onSnapchatClicked(context: Context)
    abstract fun onInstructionsClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(val state: SnapchatRepository.QuickTapToSnapState): State()
    }

}

class SettingsSharedSnapchatViewModelImpl(private val snapchatRepository: SnapchatRepository, private val navigation: ContainerNavigation): SettingsSharedSnapchatViewModel() {

    private val resumeBus = MutableSharedFlow<Unit>()

    companion object {
        private const val URL_QTTS = "https://kieronquinn.co.uk/redirect/TapTap/qtts"
    }

    override val state = resumeBus.map {
        State.Loaded(snapchatRepository.getQuickTapToSnapState())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(Unit)
        }
    }

    override fun popBackstack(delay: Boolean) {
        viewModelScope.launch {
            if(delay) delay(250L)
            navigation.navigateUpTo(R.id.nav_graph_shared_snapchat, true)
        }
    }

    override fun onSnapchatClicked(context: Context) {
        viewModelScope.launch {
            if(context.isNetworkConnected()){
                Toast.makeText(context, R.string.settings_shared_snapchat_setup_error_network, Toast.LENGTH_LONG).show()
                return@launch
            }
            snapchatRepository.applyOverride()
            delay(250L)
            Intent().apply {
               component = SnapchatAction.LAUNCH_COMPONENT
               `package` = SnapchatAction.PACKAGE_NAME
            }.also {
                context.startActivity(it)
            }
        }
    }

    override fun onInstructionsClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(URL_QTTS)
            })
        }
    }

}