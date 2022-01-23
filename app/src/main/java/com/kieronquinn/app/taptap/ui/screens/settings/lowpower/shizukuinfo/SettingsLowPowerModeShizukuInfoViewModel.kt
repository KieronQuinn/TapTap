package com.kieronquinn.app.taptap.ui.screens.settings.lowpower.shizukuinfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.utils.extensions.getPlayStoreIntentForPackage
import kotlinx.coroutines.launch
import rikka.shizuku.ShizukuProvider

abstract class SettingsLowPowerModeShizukuInfoViewModel: ViewModel() {

    abstract fun onShizukuClicked(context: Context)
    abstract fun onSuiClicked(context: Context)

}

class SettingsLowPowerModeShizukuInfoViewModelImpl(private val navigation: ContainerNavigation): SettingsLowPowerModeShizukuInfoViewModel() {

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
