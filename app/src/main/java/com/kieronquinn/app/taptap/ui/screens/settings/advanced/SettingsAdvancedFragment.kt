package com.kieronquinn.app.taptap.ui.screens.settings.advanced

import androidx.preference.PreferenceScreen
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsAdvancedFragment: BaseSettingsFragment(R.xml.settings_advanced) {

    private val viewModel by viewModel<SettingsAdvancedViewModel>()

    private val customSensitivity by lazy {
        findPreference<Preference>("advanced_custom_sensitivity")
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        customSensitivity?.bindOnClick(viewModel::onCustomSensitivityClicked, this)
    }

}