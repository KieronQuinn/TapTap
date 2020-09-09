package com.kieronquinn.app.taptap.services

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_MAIN_SWITCH
import com.kieronquinn.app.taptap.utils.isMainEnabled
import com.kieronquinn.app.taptap.utils.sharedPreferences

class TapQSTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    private fun updateState(){
        val isEnabled = isMainEnabled
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
        sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, !isMainEnabled).commit()
        updateState()
    }


}