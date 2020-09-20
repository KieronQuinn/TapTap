package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.bottomsheets.RadioButtonBottomSheetFragment
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.utils.*

class SettingsGestureFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings_gesture)
        getPreference("gesture_device_model"){
            it.setOnPreferenceClickListener {
                showDeviceModelBottomSheet()
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

    private fun showDeviceModelBottomSheet() {
        val bundle = Bundle()
        bundle.putString(RadioButtonBottomSheetFragment.KEY_TITLE, getString(R.string.setting_gesture_model))
        bundle.putStringArray(RadioButtonBottomSheetFragment.KEY_KEYS, resources.getStringArray(R.array.device_model_keys))
        bundle.putStringArray(RadioButtonBottomSheetFragment.KEY_VALUES, arrayOf(TfModel.PIXEL3XL.name, TfModel.PIXEL4.name, TfModel.PIXEL4XL.name))
        bundle.putString(RadioButtonBottomSheetFragment.KEY_PREFERENCE_KEY, SHARED_PREFERENCES_KEY_MODEL)
        val currentlySelected = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL3XL.name)
        val currentlySelectedIndex = TfModel.values().map { it.name }.indexOf(currentlySelected)
        bundle.putInt(RadioButtonBottomSheetFragment.KEY_SELECTED_INDEX, currentlySelectedIndex)

        RadioButtonBottomSheetFragment().apply {
            arguments = bundle
            show(this@SettingsGestureFragment.childFragmentManager, "bs_device_model")
        }
    }

    override fun onResume() {
        super.onResume()
        view?.post {
            getSliderPreference("gesture_sensitivity"){
                setupSlider(it.getSlider())
            }
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
                sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_SENSITIVITY, SENSITIVITY_VALUES[value.toInt()].toString())?.apply()
            }
        }
    }

}