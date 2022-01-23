package com.kieronquinn.app.taptap.ui.screens.setup.complete

import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import kotlinx.coroutines.launch

abstract class SetupCompleteViewModel: BaseSetupViewModel() {

    abstract fun onCloseClicked()

}

class SetupCompleteViewModelImpl(private val rootNavigation: RootNavigation, private val settings: TapTapSettings): SetupCompleteViewModel() {

    override fun onCloseClicked() {
        viewModelScope.launch {
            settings.hasSeenSetup.set(true)
            rootNavigation.phoenix()
        }
    }

    override fun onBackPressed(): Boolean {
        viewModelScope.launch {
            //Jump back to info screen rather than dumping back on the gesture to allow it to re-setup
            rootNavigation.navigateUpTo(R.id.setupInfoFragment)
        }
        return true
    }

}