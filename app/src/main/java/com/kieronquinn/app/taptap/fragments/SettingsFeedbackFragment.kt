package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import com.kieronquinn.app.taptap.R

class SettingsFeedbackFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings_feedback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

}