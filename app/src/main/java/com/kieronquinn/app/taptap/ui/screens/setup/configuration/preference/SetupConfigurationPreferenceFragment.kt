package com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference

import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SENSITIVITY_VALUES
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_SENSITIVITY
import com.kieronquinn.app.taptap.models.getStringValue
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.ui.preferences.SliderPreference
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.utils.extensions.sharedPreferences
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference.modelpicker.SetupConfigurationPreferenceModelPickerBottomSheetDialogFragment
import com.kieronquinn.app.taptap.utils.extensions.indexOfOrNull
import org.koin.android.viewmodel.ext.android.viewModel

class SetupConfigurationPreferenceFragment: BaseSettingsFragment(R.xml.setup_settings_adjustment) {

    private val viewModel by viewModel<SetupConfigurationPreferenceViewModel>()

    private val sliderPreference by lazy {
        findPreference<SliderPreference>("gesture_sensitivity")
    }

    private val modelPreference by lazy {
        findPreference<Preference>("gesture_device_model")
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        viewModel.getSensitivity().observe(viewLifecycleOwner) {
            val newValue = SENSITIVITY_VALUES.indexOfOrNull(it)?.toFloat() ?: 5f
            sliderPreference?.getSlider()?.value = newValue
        }
        viewModel.getModel(requireContext()).observe(viewLifecycleOwner) { model ->
            modelPreference?.run {
                summary = getString(
                    R.string.setup_gesture_configuration_adjustment_model_desc,
                    model.getStringValue(context)
                )
                setOnPreferenceClickListener {
                    showDeviceModelBottomSheet()
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        listView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        view?.post {
            setupSlider(sliderPreference!!.getSlider())
        }
    }

    private fun setupSlider(slider: Slider){
        val currentPreference = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.toFloatOrNull() ?: 0.05f
        slider.apply {
            valueFrom = 0f
            valueTo = 10f
            stepSize = 1f
            value = SENSITIVITY_VALUES.indexOfOrNull(currentPreference)?.toFloat() ?: 5f
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
                viewModel.onSensitivityChanged(SENSITIVITY_VALUES[value.toInt()])
            }
        }
    }

    private fun showDeviceModelBottomSheet() {
        SetupConfigurationPreferenceModelPickerBottomSheetDialogFragment().show(childFragmentManager, "bs_device_model")
    }

}