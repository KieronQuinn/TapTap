package com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_SENSITIVITY
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SetupConfigurationPreferenceViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    private val flowSharedPreferences = tapSharedPreferences.flowSharedPreferences

    fun getModel(context: Context): LiveData<TfModel> {
        return flowSharedPreferences.getEnum(SHARED_PREFERENCES_KEY_MODEL, context.getDefaultTfModel()).asFlow().asLiveData()
    }

    fun getSensitivity(): LiveData<Float> = MediatorLiveData<Float>().apply {
        addSource(flowSharedPreferences.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05f").asFlow().asLiveData()){
            update(it.toFloat())
        }
    }

    fun onSensitivityChanged(newValue: Float){
        tapSharedPreferences.sensitivity = newValue
    }

}