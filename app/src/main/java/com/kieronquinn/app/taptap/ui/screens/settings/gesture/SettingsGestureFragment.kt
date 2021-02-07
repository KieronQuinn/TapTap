package com.kieronquinn.app.taptap.ui.screens.settings.gesture

import androidx.preference.PreferenceScreen
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.ui.preferences.SliderPreference
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsGestureFragment: BaseSettingsFragment(R.xml.settings_gesture) {

    private val viewModel by viewModel<SettingsGestureViewModel>()

    private val deviceModelPreference by lazy {
        findPreference<Preference>("gesture_device_model")
    }

    private val sensitivityPreference by lazy {
        findPreference<SliderPreference>("gesture_sensitivity")
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        with(viewModel){
            deviceModelPreference?.bindOnClick(::onDeviceModelClicked, this@SettingsGestureFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.post {
            sensitivityPreference?.getSlider()?.let {
                setupSlider(it)
            }
            viewModel.gestureSensitivity.observe(viewLifecycleOwner){
                sensitivityPreference?.getSlider()?.value = it
            }
        }
    }

    private fun setupSlider(slider: Slider){
        slider.apply {
            valueFrom = 0f
            valueTo = 10f
            stepSize = 1f
            setLabelFormatter {
                getString(when {
                    it < 2 -> R.string.slider_sensitivity_very_low
                    it < 4 -> R.string.slider_sensitivity_low
                    it < 6 -> R.string.slider_sensitivity_normal
                    it < 8 -> R.string.slider_sensitivity_high
                    else -> R.string.slider_sensitivity_very_high
                })
            }
            addOnChangeListener { slider, value, fromUser ->
                if(fromUser) {
                    viewModel.onSensitivityChanged(TapSharedPreferences.SENSITIVITY_VALUES[value.toInt()])
                }
            }
        }
    }

}