package com.kieronquinn.app.taptap.ui.screens.setup.upgrade

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.backuprestore.RestoreRepository
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SetupUpgradeViewModel: BaseSetupViewModel() {

    abstract fun startUpgrade(context: Context)

}

class SetupUpgradeViewModelImpl(private val restoreRepository: RestoreRepository, private val rootNavigation: RootNavigation): SetupUpgradeViewModel() {

    private var hasStarted = false

    override fun startUpgrade(context: Context) {
        if(hasStarted) return
        hasStarted = true
        viewModelScope.launch {
            restoreRepository.upgradeSettings(context)
            rootNavigation.phoenix()
        }
    }

}