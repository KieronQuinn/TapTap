package com.kieronquinn.app.taptap.ui.screens.settings.gesture.model

import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SettingsGestureModelBottomSheetViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    fun onModelSelected(tfModel: TfModel){
        tapSharedPreferences.model = tfModel.name
    }

}