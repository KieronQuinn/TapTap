package com.kieronquinn.app.taptap.ui.screens.disablecolumbus

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.utils.extensions.getColumbusSettingAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class DisableColumbusViewModel: ViewModel() {

    abstract val phoenixBus: Flow<Unit>

    abstract fun onOpenSettingsClicked()
    abstract fun phoenix()
    abstract fun onLinkClicked(url: String)

}

class DisableColumbusViewModelImpl(context: Context, private val navigation: RootNavigation): DisableColumbusViewModel() {

    companion object {
        private const val PACKAGE_SYSTEM_SETTINGS = "com.android.settings"
    }

    private val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_SYSTEM_SETTINGS)

    override val phoenixBus = context.getColumbusSettingAsFlow().drop(1).map {  }

    override fun onOpenSettingsClicked() {
        viewModelScope.launch {
            navigation.navigate(intent ?: return@launch)
        }
    }

    override fun phoenix() {
        viewModelScope.launch {
            navigation.phoenix()
        }
    }

    override fun onLinkClicked(url: String) {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

}