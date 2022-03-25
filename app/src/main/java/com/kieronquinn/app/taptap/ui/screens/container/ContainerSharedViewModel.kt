package com.kieronquinn.app.taptap.ui.screens.container

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.service.TapTapServiceRouter
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.demomode.DemoModeRepository
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabase
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import com.kieronquinn.app.taptap.utils.extensions.getColumbusSettingAsFlow
import com.kieronquinn.app.taptap.utils.extensions.instantCombine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class ContainerSharedViewModel: ViewModel() {

    abstract val isServiceRunning: StateFlow<Boolean>
    abstract val fabState: StateFlow<FabState>
    abstract val fabClicked: Flow<FabState.FabAction>
    abstract val snackbarBus: Flow<CharSequence>
    abstract val columbusSettingPhoenixBus: Flow<Unit>
    abstract fun toggleServiceEnabledState(context: Context)
    abstract fun checkServiceState(context: Context)
    abstract fun restartService(context: Context)
    abstract fun setFabState(fabState: FabState)
    abstract fun onFabClicked(fabAction: FabState.FabAction)
    abstract fun showSnackbar(text: CharSequence)
    abstract fun setSuppressColumbusRestart(suppress: Boolean)

    sealed class FabState {
        object Hidden: FabState()
        data class Shown(val action: FabAction): FabState()

        enum class FabAction(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
            ADD_ACTION(R.drawable.ic_fab_action_add, R.string.fab_add_action),
            DELETE(R.drawable.ic_fab_action_delete, R.string.fab_remove_action),
            ADD_GATE(R.drawable.ic_fab_action_add, R.string.fab_add_gate),
            ADD_REQUIREMENT(R.drawable.ic_fab_action_add, R.string.fab_add_requirement),
            RESTORE_BACKUP(R.drawable.ic_backup_restore_restore, R.string.settings_backup_restore_restore),
            DOWNLOAD(R.drawable.ic_download, R.string.settings_update_fab)
        }
    }

}

class ContainerSharedViewModelImpl(context: Context, private val database: TapTapDatabase, private val settings: TapTapSettings, private val demoModeRepository: DemoModeRepository, serviceRouter: TapTapServiceRouter): ContainerSharedViewModel() {

    private val serviceStateCheck = MutableSharedFlow<Unit>()
    private var suppressColumbusRestart = false

    private val _isServiceRunning = instantCombine(serviceRouter.serviceStartBus, serviceRouter.serviceStopBus, serviceStateCheck).map {
        getServiceRunning(context)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, getServiceRunning(context))

    override val isServiceRunning = _isServiceRunning
    override val columbusSettingPhoenixBus = context.getColumbusSettingAsFlow().drop(1).filterNot { suppressColumbusRestart }.map {  }

    private val _fabState = MutableStateFlow<FabState>(FabState.Hidden)
    override val fabState = _fabState.asStateFlow()

    private val _fabClicked = MutableSharedFlow<FabState.FabAction>()
    override val fabClicked = _fabClicked.asSharedFlow()

    private val _snackbarBus = MutableSharedFlow<CharSequence>()
    override val snackbarBus = _snackbarBus.asSharedFlow()

    override fun toggleServiceEnabledState(context: Context) {
        viewModelScope.launch {
            val newState = !_isServiceRunning.value
            settings.serviceEnabled.set(newState)
            TapTapForegroundService.stop(context)
            if(newState) {
                TapTapForegroundService.start(context)
            }
            delay(100)
            checkServiceState(context)
        }
    }

    override fun checkServiceState(context: Context) {
        viewModelScope.launch {
            serviceStateCheck.emit(Unit)
        }
    }

    override fun restartService(context: Context) {
        viewModelScope.launch {
            restartServiceLocked(context)
        }
    }

    /**
     *  Start calls are locked for 2.5 seconds after launch (theoretically should be 5s, but 2.5s
     *  seems to be enough 99% of the time), to prevent ForegroundServiceDidNotStartInTimeException
     *  from calling stopService before startForegroundService
     */

    private val containerServiceLock = Mutex()

    private suspend fun restartServiceLocked(context: Context) = containerServiceLock.withLock {
        if (demoModeRepository.isDemoModeEnabled() || settings.serviceEnabled.get()) {
            TapTapForegroundService.start(context, true)
            delay(2500L)
        }
    }

    override fun setFabState(fabState: FabState) {
        viewModelScope.launch {
            _fabState.emit(fabState)
        }
    }

    override fun onFabClicked(fabAction: FabState.FabAction) {
        viewModelScope.launch {
            _fabClicked.emit(fabAction)
        }
    }

    override fun showSnackbar(text: CharSequence) {
        viewModelScope.launch {
            _snackbarBus.emit(text)
        }
    }

    override fun setSuppressColumbusRestart(suppress: Boolean) {
        suppressColumbusRestart = suppress
    }

    private fun getServiceRunning(context: Context): Boolean {
        return TapTapForegroundService.isRunning(context)
    }

    init {
        context.broadcastReceiverAsFlow(TapTapForegroundService.ACTION_SERVICE_UPDATE).apply {
            viewModelScope.launch {
                collect {
                    checkServiceState(context)
                }
            }
        }
        restartService(context)
        viewModelScope.launch {
            database.restartBus.collect {
                restartService(context)
            }
        }
    }

}