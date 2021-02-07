package com.kieronquinn.app.taptap.ui.screens.settings.advanced

import androidx.fragment.app.Fragment
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SettingsAdvancedViewModel: BaseViewModel() {

    fun onCustomSensitivityClicked(fragment: Fragment){
        fragment.navigate(SettingsAdvancedFragmentDirections.actionSettingsAdvancedFragmentToSettingsAdvancedCustomSensitivityBottomSheetDialogFragment())
    }

}