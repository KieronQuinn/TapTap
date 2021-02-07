package com.kieronquinn.app.taptap.ui.screens.settings.main

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceScreen
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.preferences.ChipPreference
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsMainFragment: BaseSettingsFragment(R.xml.settings_main) {

    private val viewModel by viewModel<SettingsMainViewModel>()

    private val accessibilityPreference by lazy {
        findPreference<Preference>("accessibility")
    }

    private val gestureAccessibilityPreference by lazy {
        findPreference<Preference>("accessibility_gesture")
    }

    private val batteryOptimisationPreference by lazy {
        findPreference<Preference>("battery_optimisation")
    }

    private val gesturePreference by lazy {
        findPreference<Preference>("gesture")
    }

    private val actionsPreference by lazy {
        findPreference<Preference>("actions")
    }

    private val actionsTriplePreference by lazy {
        findPreference<Preference>("actions_triple")
    }

    private val gatesPreference by lazy {
        findPreference<Preference>("gates")
    }

    private val feedbackPreference by lazy {
        findPreference<Preference>("feedback")
    }

    private val backupRestorePreference by lazy {
        findPreference<Preference>("backup_restore")
    }

    private val advancedPreference by lazy {
        findPreference<Preference>("advanced")
    }

    private val aboutAboutPreference by lazy {
        findPreference<ChipPreference>("about_about")
    }

    private val aboutBatteryPreference by lazy {
        findPreference<Preference>("about_battery")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        //Hide the accessibility and battery preferences by default so they only animate for users with them required
        gestureAccessibilityPreference?.isVisible = false
        batteryOptimisationPreference?.isVisible = false
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        with(viewModel) {
            isAccessibilityServiceEnabled.observe(viewLifecycleOwner) {
                accessibilityPreference?.let { preference ->
                    setAccessibilityServiceState(preference, it)
                }
            }
            gestureAccessibilityServiceState.observe(viewLifecycleOwner) {
                gestureAccessibilityPreference?.let { preference ->
                    setGestureAccessibilityServiceState(preference, it)
                }
            }
            isBatteryOptimisationDisabled.observe(viewLifecycleOwner) {
                batteryOptimisationPreference?.isVisible = it
            }
            version.observe(viewLifecycleOwner){
                aboutAboutPreference?.title = it
            }
            aboutSummary.observe(viewLifecycleOwner){
                aboutAboutPreference?.summary = it
            }
            getVersion(requireContext())
            getAboutSummary(requireContext())
            accessibilityPreference?.bindOnClick(::onAccessibilityClicked, requireContext())
            gestureAccessibilityPreference?.bindOnClick(::onGestureAccessibilityClicked, requireContext())
            batteryOptimisationPreference?.bindOnClick(::onBatteryOptimisationClicked, requireContext())
            gesturePreference?.bindOnClick(::onGestureClicked, this@SettingsMainFragment)
            actionsPreference?.bindOnClick(::onActionsClicked, this@SettingsMainFragment)
            actionsTriplePreference?.bindOnClick(::onActionsTripleClicked, this@SettingsMainFragment)
            gatesPreference?.bindOnClick(::onGatesClicked, this@SettingsMainFragment)
            feedbackPreference?.bindOnClick(::onFeedbackClicked, this@SettingsMainFragment)
            advancedPreference?.bindOnClick(::onAdvancedClicked, this@SettingsMainFragment)
            backupRestorePreference?.bindOnClick(::onBackupRestoreClicked, this@SettingsMainFragment)
            aboutBatteryPreference?.bindOnClick(::onBatteryClicked, this@SettingsMainFragment)
            aboutAboutPreference?.run {
                setOnPreferenceClickListener {
                    viewModel.onAboutClicked(this@SettingsMainFragment)
                    true
                }
                val donateChip = ChipPreference.PreferenceChip(R.color.icon_circle_7, R.string.donate, R.drawable.ic_donate){
                    viewModel.onDonateClicked(requireContext())
                }
                val twitterChip = ChipPreference.PreferenceChip(R.color.icon_circle_8, R.string.twitter, R.drawable.ic_twitter){
                    viewModel.onTwitterClicked(requireContext())
                }
                setChips(arrayOf(donateChip, twitterChip))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        with(viewModel) {
            getAccessibilityServiceState(requireContext())
            getGestureAccessibilityServiceState(requireContext())
            getBatteryOptimisationState(requireContext())
        }
    }

    private fun setAccessibilityServiceState(preference: Preference, enabled: Boolean) = preference.run {
        if(enabled){
            title = getString(R.string.accessibility_info_on)
            summary = getString(R.string.accessibility_info_on_desc_2)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_accessibility_check_round)
            setBackgroundTint(null)
        }else{
            title = getString(R.string.accessibility_info_off)
            summary = getString(R.string.accessibility_info_off_desc)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_accessibility_cross_round)
            setBackgroundTint(ContextCompat.getColor(context, R.color.accessibility_cross_circle))
        }
    }

    private fun setGestureAccessibilityServiceState(preference: Preference, state: SettingsMainViewModel.GestureAccessibilityServiceState) = preference.run {
        isVisible = state != SettingsMainViewModel.GestureAccessibilityServiceState.HIDDEN
        when(state){
            SettingsMainViewModel.GestureAccessibilityServiceState.NEEDS_ENABLE -> {
                isVisible = true
                title = getString(R.string.accessibility_info_off_gesture)
                summary = getString(R.string.accessibility_info_off_gesture_desc)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_accessibility_cross_round)
            }
            SettingsMainViewModel.GestureAccessibilityServiceState.NEEDS_DISABLE -> {
                title = getString(R.string.accessibility_info_on_gesture)
                summary = getString(R.string.accessibility_info_on_gesture_desc)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_warning_round)
            }
            else -> {}
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed(this)

}