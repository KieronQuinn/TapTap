package com.kieronquinn.app.taptap.core.services

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import org.koin.android.ext.android.inject

class TapQSTile : TileService() {

    private val tapSharedPreferences by inject<TapSharedPreferences>()

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    private fun updateState(){
        val isEnabled = tapSharedPreferences.isMainEnabled
        qsTile.label = getString(R.string.app_name)
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_taptap_logo)
        qsTile.state = if(isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateState()
    }

    private val isClickable : Boolean
        get() = qsTile.state != Tile.STATE_UNAVAILABLE

    override fun onClick() {
        super.onClick()
        if(!isClickable) return
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
        tapSharedPreferences.isMainEnabled = !tapSharedPreferences.isMainEnabled
        updateState()
    }


}