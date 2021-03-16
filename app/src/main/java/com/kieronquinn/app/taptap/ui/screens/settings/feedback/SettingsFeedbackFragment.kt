package com.kieronquinn.app.taptap.ui.screens.settings.feedback

import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.ui.preferences.SwitchPreference
import com.kieronquinn.app.taptap.utils.extensions.observe
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFeedbackFragment: BaseSettingsFragment(R.xml.settings_feedback) {

    private val viewModel by viewModel<SettingsFeedbackViewModel>()

    private val feedbackVibrateEffectPreference by lazy {
        findPreference<SeekBarPreference>("feedback_vibrate_duration")
    }
    private val feedbackVibratePreference by lazy {
        findPreference<SwitchPreference>("feedback_vibrate")
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        feedbackVibratePreference?.setOnPreferenceChangeListener { preference, newValue ->
            feedbackVibrateEffectPreference?.isEnabled = newValue as Boolean
            true
        }
    }

    override fun onResume() {
        super.onResume()
        view?.post {
            feedbackVibrateEffectPreference?.let {
                setupSlider(it)
            }
            viewModel.feedbackVibrationEffect.observe(viewLifecycleOwner){
                feedbackVibrateEffectPreference?.value = it
            }
        }
    }

    private fun setupSlider(seekBarPreference: SeekBarPreference){
        seekBarPreference.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                viewModel.onVibrationEffectChanged(newValue as Int)
                true
            }
        }
    }


}