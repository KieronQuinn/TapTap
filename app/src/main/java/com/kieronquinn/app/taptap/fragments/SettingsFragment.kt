package com.kieronquinn.app.taptap.fragments

import android.app.NotificationManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.fragments.bottomsheets.GenericBottomSheetFragment
import com.kieronquinn.app.taptap.preferences.Preference
import com.kieronquinn.app.taptap.services.TapForegroundService
import com.kieronquinn.app.taptap.utils.EXTRA_FRAGMENT_ARG_KEY
import com.kieronquinn.app.taptap.utils.EXTRA_SHOW_FRAGMENT_ARGUMENTS
import com.kieronquinn.app.taptap.utils.Links
import com.kieronquinn.app.taptap.utils.isAccessibilityServiceEnabled
import java.lang.RuntimeException

class SettingsFragment : BaseSettingsFragment() {

    private val TAG = "SettingsFragment"
    private val returnReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            context?.unregisterReceiver(this)
            try {
                startActivity(Intent(context, SettingsActivity::class.java))
            }catch (e: RuntimeException){
                //Fragment isn't attached
            }
        }
    }

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
        getPreference("actions_triple"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsActionTripleFragment)
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
        getPreference("advanced"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsAdvancedFragment)
                true
            }
        }
        getPreference("battery_optimisation"){
            it.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                }
                startActivity(intent)
                true
            }
        }
        getPreference("about_battery"){
            it.setOnPreferenceClickListener {
                showBatteryInfoBottomSheet()
                true
            }
        }

        findPreference<Preference>("about_about")?.apply {
            title = getString(R.string.about, getString(R.string.app_name), BuildConfig.VERSION_NAME)
            summary = getString(R.string.about_summary, getString(R.string.about_summary_contributors))
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
                it.summary = getString(R.string.accessibility_info_on_desc_2)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_check_round)
            }else{
                it.title = getString(R.string.accessibility_info_off)
                it.summary = getString(R.string.accessibility_info_off_desc)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_cross_round)
            }
            it.setOnPreferenceClickListener { _ ->
                context?.registerReceiver(returnReceiver, IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START))
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    val bundle = Bundle()
                    val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapAccessibilityService::class.java.name).flattenToString()
                    bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
                    putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                    putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
                })
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                Toast.makeText(it.context, R.string.accessibility_info_toast, Toast.LENGTH_LONG).show()
                true
            }
        }
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        getPreference("battery_optimisation"){
            it.isVisible = !powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
        }

        view?.post {
            getPreference("accessibility"){
                if(isServiceEnabled) {
                    it.setBackgroundTint(null)
                }else{
                    it.setBackgroundTint(ContextCompat.getColor(it.context, R.color.accessibility_cross_circle))
                }
            }
            getPreference("battery_optimisation"){
                it.setBackgroundTint(ContextCompat.getColor(it.context, R.color.icon_circle_10))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        (activity as? SettingsActivity)?.updateChecker?.newUpdate?.run {
            observe(this@SettingsFragment, Observer {
                menu.findItem(R.id.menu_update).isVisible = it != null
            })
            menu.findItem(R.id.menu_update).isVisible = this.value != null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_beta -> {
                GenericBottomSheetFragment.create(getString(R.string.bs_beta), R.string.bs_beta_title, android.R.string.ok).show(childFragmentManager, "bs_alpha")
            }
            R.id.menu_update -> {
                (activity as? SettingsActivity)?.showUpdateBottomSheet()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBatteryInfoBottomSheet(){
        GenericBottomSheetFragment.create(getString(R.string.bs_battery_content), R.string.battery_and_optimisation, android.R.string.ok, R.string.bs_battery_positive, "https://dontkillmyapp.com/").show(childFragmentManager, "bs_alpha")
    }

}