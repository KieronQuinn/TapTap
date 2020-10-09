package com.kieronquinn.app.taptap.fragments.setup

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.SettingsGestureModelDialogFragment
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.models.getStringValue
import com.kieronquinn.app.taptap.utils.*

class GestureConfigurationPreferenceFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.setup_settings_adjustment)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        listView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        view?.post {
            getSliderPreference("gesture_sensitivity"){
                setupSlider(it.getSlider())
            }
        }
        refreshModelPreference()
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun refreshModelPreference(){
        getPreference("gesture_device_model"){
            val default = it.context.getDefaultTfModel()
            val model = TfModel.valueOf(sharedPreferences?.getString(SHARED_PREFERENCES_KEY_MODEL, default.name) ?: default.name)
            it.summary = getString(R.string.setup_gesture_configuration_adjustment_model_desc, model.getStringValue(it.context))
            it.setOnPreferenceClickListener {
                showDeviceModelBottomSheet()
                true
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

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences, key: String) {
        if(key == SHARED_PREFERENCES_KEY_MODEL){
            refreshModelPreference()
        }
    }

    private fun showDeviceModelBottomSheet() {
        MaterialBottomSheetDialogFragment.create(SettingsGestureModelDialogFragment(), childFragmentManager, "bs_device_model"){}
    }

}