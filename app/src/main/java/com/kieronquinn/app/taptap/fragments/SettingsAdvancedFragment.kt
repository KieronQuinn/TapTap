package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.bottomsheets.CustomSensitivityBottomSheet
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.utils.getPreference

class SettingsAdvancedFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings_advanced)
        getPreference("advanced_custom_sensitivity"){
            it.setOnPreferenceClickListener {
                MaterialBottomSheetDialogFragment.create(CustomSensitivityBottomSheet(), childFragmentManager, "bs_advanced_custom_sensitivity"){}
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

}