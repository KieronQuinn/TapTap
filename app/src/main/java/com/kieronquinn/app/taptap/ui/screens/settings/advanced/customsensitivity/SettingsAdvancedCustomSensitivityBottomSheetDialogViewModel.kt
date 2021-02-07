package com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SettingsAdvancedCustomSensitivityBottomSheetDialogViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    private val flowSharedPreferences = tapSharedPreferences.flowSharedPreferences

    private val sensitivity = flowSharedPreferences.getString(TapSharedPreferences.SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05").asFlow().asLiveData()

    val currentValue = MediatorLiveData<String>().apply {
        addSource(sensitivity){
            update(it)
        }
    }

    fun onPositiveButtonClicked(fragment: BottomSheetDialogFragment){
        tapSharedPreferences.sensitivity = currentValue.value?.toFloatOrNull() ?: 0.05f
        fragment.findNavController().navigateUp()
    }

    fun onNeutralButtonClicked(fragment: BottomSheetDialogFragment){
        tapSharedPreferences.sensitivity = 0.05f
        fragment.findNavController().navigateUp()
    }

}