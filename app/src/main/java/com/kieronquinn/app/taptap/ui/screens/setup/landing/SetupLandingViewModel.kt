package com.kieronquinn.app.taptap.ui.screens.setup.landing

import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.NavigationEvent
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.decision.DecisionFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class SetupLandingViewModel: BaseSetupViewModel() {

    abstract fun onStartClicked()
    abstract fun onSkipClicked()

}

class SetupLandingViewModelImpl(private val settings: TapTapSettings, private val navigation: RootNavigation): SetupLandingViewModel() {

    override fun onStartClicked() {
        viewModelScope.launch {
            navigation.navigate(SetupLandingFragmentDirections.actionSetupLandingFragmentToSetupInfoFragment())
        }
    }

    override fun onSkipClicked() {
        viewModelScope.launch {
            settings.hasSeenSetup.set(true)
            if(!settings.serviceEnabled.exists()){
                //Enable the service on skip if it's not been disabled manually
                settings.serviceEnabled.set(true)
            }
            navigation.phoenix()
        }
    }

}