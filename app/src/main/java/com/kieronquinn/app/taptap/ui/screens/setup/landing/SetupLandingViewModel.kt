package com.kieronquinn.app.taptap.ui.screens.setup.landing

import androidx.fragment.app.Fragment
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SetupLandingViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    fun onGetStartedClicked(fragment: Fragment){
        fragment.navigate(SetupLandingFragmentDirections.actionSetupLandingFragmentToSetupConfigurationFragment())
    }

    fun onSkipSetupClicked(fragment: Fragment){
        tapSharedPreferences.hasSeenSetup = true
        //fragment.navigate(SetupLandingFragmentDirections.actionSetupLandingFragmentToSettingsActivity())
    }

}