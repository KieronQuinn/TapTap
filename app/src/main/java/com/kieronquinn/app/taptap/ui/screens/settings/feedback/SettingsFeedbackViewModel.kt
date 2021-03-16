package com.kieronquinn.app.taptap.ui.screens.settings.feedback

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SettingsFeedbackViewModel(private val tapSharedPreferences: TapSharedPreferences) :
    BaseViewModel() {

    private val flowSharedPreferences = tapSharedPreferences.flowSharedPreferences

    private val feedbackVibrationEffectPreference = flowSharedPreferences.getString(
        TapSharedPreferences.SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE_DURATION,
        "100"
    ).asFlow().asLiveData()

    val feedbackVibrationEffect = MediatorLiveData<Int>().apply {
        addSource(feedbackVibrationEffectPreference) {
            update(it.toIntOrNull())
        }
    }

    fun onVibrationEffectChanged(vibrationEffect: Int) {
        tapSharedPreferences.vibrationEffect = vibrationEffect
    }
}