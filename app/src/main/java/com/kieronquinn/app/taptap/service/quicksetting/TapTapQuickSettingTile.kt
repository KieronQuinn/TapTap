package com.kieronquinn.app.taptap.service.quicksetting

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.service.TapTapServiceRouter
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.invert
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.app.taptap.utils.lifecycle.LifecycleTileService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.android.ext.android.inject

class TapTapQuickSettingTile: LifecycleTileService() {

    private val reload = MutableSharedFlow<Unit>()
    private val settings by inject<TapTapSettings>()
    private val serviceRouter by inject<TapTapServiceRouter>()

    private val icon by lazy {
        Icon.createWithResource(this, R.drawable.ic_taptap_logo)
    }

    private val label by lazy {
        getString(R.string.app_name)
    }

    data class State(val state: Int, @StringRes val subtitleRes: Int)

    private val state = combine(reload, settings.serviceEnabled.asFlow(), serviceRouter.serviceStartBus) { _, _, _ ->
        getState()
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)

    private suspend fun getState(): State {
        val enabled = settings.serviceEnabled.get()
        val serviceRunning = TapTapForegroundService.isRunning(applicationContext)
        val state = when {
            !enabled -> Tile.STATE_INACTIVE
            !serviceRunning -> Tile.STATE_INACTIVE
            else -> Tile.STATE_ACTIVE
        }
        val subtitle = when {
            !enabled -> R.string.qs_subtitle_disabled
            !serviceRunning -> R.string.qs_subtitle_not_running
            else -> R.string.qs_subtitle_running
        }
        return State(state, subtitle)
    }

    private fun setupState(){
        lifecycle.whenCreated {
            state.collect {
                handleState(it ?: return@collect)
            }
        }
    }

    private fun handleState(state: State){
        qsTile.state = state.state
        qsTile.icon = icon
        qsTile.label = label
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(state.subtitleRes)
        }
        qsTile.updateTile()
    }

    init {
        setupState()
        triggerUpdate()
    }

    private fun triggerUpdate() = lifecycle.whenCreated {
        reload.emit(Unit)
    }

    override fun onClick() {
        super.onClick()
        lifecycle.whenCreated {
            settings.serviceEnabled.invert()
            TapTapForegroundService.stop(applicationContext)
            if(settings.serviceEnabled.get()){
                TapTapForegroundService.start(applicationContext)
            }
        }
    }

}