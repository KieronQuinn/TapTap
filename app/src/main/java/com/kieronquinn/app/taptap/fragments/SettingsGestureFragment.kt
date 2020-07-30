package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.bottomsheets.RadioButtonBottomSheetFragment
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.utils.sharedPreferences

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

}