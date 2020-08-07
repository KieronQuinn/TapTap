package com.kieronquinn.app.taptap.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.fragments.bottomsheets.GenericBottomSheetFragment
import com.kieronquinn.app.taptap.preferences.Preference
import com.kieronquinn.app.taptap.utils.Links
import com.kieronquinn.app.taptap.utils.isAccessibilityServiceEnabled

class SettingsFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings_main)
        getPreference("gesture"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsGestureFragment)
                true
            }
        }
        getPreference("actions"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsActionFragment)
                true
            }
        }
        getPreference("gates"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsGateFragment)
                true
            }
        }
        getPreference("feedback"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsFeedbackFragment)
                true
            }
        }

        findPreference<Preference>("about_about")?.apply {
            title = getString(R.string.about, getString(R.string.app_name), BuildConfig.VERSION_NAME)
        }
        findPreference<Preference>("about_libraries")?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.libraries))
                true
            }
        }
        context?.let { context ->
            Links.setupPreference(context, preferenceScreen, "about_github", Links.LINK_GITHUB)
            Links.setupPreference(context, preferenceScreen, "about_xda", Links.LINK_XDA)
            Links.setupPreference(context, preferenceScreen, "about_donate", Links.LINK_DONATE)
            Links.setupPreference(context, preferenceScreen, "about_twitter", Links.LINK_TWITTER)
        }
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        setHomeAsUpEnabled(false)
        val isServiceEnabled = isAccessibilityServiceEnabled(requireContext(), TapAccessibilityService::class.java)
        getPreference("accessibility"){
            if(isServiceEnabled){
                it.title = getString(R.string.accessibility_info_on)
                it.summary = getString(R.string.accessibility_info_on_desc)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_check_round)
            }else{
                it.title = getString(R.string.accessibility_info_off)
                it.summary = getString(R.string.accessibility_info_off_desc)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_cross_round)
            }
            it.setOnPreferenceClickListener { _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(it.context, R.string.accessibility_info_toast, Toast.LENGTH_LONG).show()
                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_alpha -> {
                GenericBottomSheetFragment.create(getString(R.string.bs_alpha), R.string.bs_alpha_title, android.R.string.ok).show(childFragmentManager, "bs_alpha")
            }
        }
        return super.onOptionsItemSelected(item)
    }

}