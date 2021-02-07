package com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference.modelpicker

import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SetupConfigurationPreferenceModelPickerBottomSheetViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    fun onModelSelected(model: TfModel){
        tapSharedPreferences.model = model.name
    }

}