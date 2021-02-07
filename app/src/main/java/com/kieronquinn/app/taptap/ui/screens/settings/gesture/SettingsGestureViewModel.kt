package com.kieronquinn.app.taptap.ui.screens.settings.gesture

import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.utils.extensions.indexOfOrNull

class SettingsGestureViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    private val flowSharedPreferences = tapSharedPreferences.flowSharedPreferences

    private val gestureSensitivityPreference = flowSharedPreferences.getString(TapSharedPreferences.SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05").asFlow().asLiveData()

    val gestureSensitivity = MediatorLiveData<Float>().apply {
        addSource(gestureSensitivityPreference){
            update(TapSharedPreferences.SENSITIVITY_VALUES.indexOfOrNull(it.toFloatOrNull())?.toFloat() ?: 5f)
        }
    }

    fun onSensitivityChanged(sensitivity: Float){
        tapSharedPreferences.sensitivity = sensitivity
    }

    fun onDeviceModelClicked(fragment: Fragment){
        fragment.navigate(SettingsGestureFragmentDirections.actionSettingsGestureFragmentToSettingsGestureModelBottomSheetDialogFragment())
    }

}