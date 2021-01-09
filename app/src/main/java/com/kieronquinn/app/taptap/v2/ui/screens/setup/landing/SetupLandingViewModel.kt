package com.kieronquinn.app.taptap.v2.ui.screens.setup.landing

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.navigate
import com.kieronquinn.app.taptap.utils.restart
import com.kieronquinn.app.taptap.v2.components.base.BaseViewModel

class SetupLandingViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    fun onGetStartedClicked(fragment: Fragment){
        fragment.navigate(SetupLandingFragmentDirections.actionSetupLandingFragmentToSetupConfigurationFragment())
    }

    fun onSkipSetupClicked(fragment: Fragment){
        tapSharedPreferences.hasSeenSetup = true
        fragment.navigate(SetupLandingFragmentDirections.actionSetupLandingFragmentToSettingsActivity())
    }

}