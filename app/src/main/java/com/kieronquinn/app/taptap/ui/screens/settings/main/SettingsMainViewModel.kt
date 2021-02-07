package com.kieronquinn.app.taptap.ui.screens.settings.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.ui.activities.SettingsActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SettingsMainViewModel(private val fileRepository: TapFileRepository): BaseViewModel() {

    companion object {
        private val ACCESSIBILITY_SERVICE_COMPONENT = ComponentName(BuildConfig.APPLICATION_ID, TapAccessibilityService::class.java.name)
        val GESTURE_ACCESSIBILITY_SERVICE_COMPONENT = ComponentName(BuildConfig.APPLICATION_ID, TapGestureAccessibilityService::class.java.name)
        private const val LINK_DONATE = "https://kieronquinn.co.uk/redirect/TapTap/donate"
        private const val LINK_TWITTER = "https://kieronquinn.co.uk/redirect/TapTap/twitter"
    }

    val isAccessibilityServiceEnabled = MutableLiveData<Boolean>()
    val gestureAccessibilityServiceState = MutableLiveData<GestureAccessibilityServiceState>()
    val isBatteryOptimisationDisabled = MutableLiveData<Boolean>()
    val version = MutableLiveData<String>()
    val aboutSummary = MutableLiveData<String>()

    fun getAccessibilityServiceState(context: Context) = viewModelScope.launch {
        Settings.Secure::class.java.getSettingAsFlow<String>(
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            context.contentResolver,
            true
        ).collect {
            isAccessibilityServiceEnabled.update((
                it?.doesContainComponentName(
                    ACCESSIBILITY_SERVICE_COMPONENT
                ) ?: false)
            )
        }
    }

    fun getGestureAccessibilityServiceState(context: Context) = viewModelScope.launch {
        Settings.Secure::class.java.getSettingAsFlow<String>(
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            context.contentResolver,
            true
        ).collect {
            val isEnabled = it?.doesContainComponentName(GESTURE_ACCESSIBILITY_SERVICE_COMPONENT) ?: false
            val isRequired = fileRepository.isGestureServiceRequired(context)
            gestureAccessibilityServiceState.update(when {
                isRequired && !isEnabled -> GestureAccessibilityServiceState.NEEDS_ENABLE
                !isRequired && isEnabled -> GestureAccessibilityServiceState.NEEDS_DISABLE
                else -> GestureAccessibilityServiceState.HIDDEN
            })
        }
    }

    fun getBatteryOptimisationState(context: Context) = viewModelScope.launch {
        getBatteryOptimisation(context).collect {
            isBatteryOptimisationDisabled.update(!it)
        }
    }

    fun getVersion(context: Context) = viewModelScope.launch {
        version.update(context.getString(R.string.about, context.getString(R.string.app_name), BuildConfig.VERSION_NAME))
    }

    fun getAboutSummary(context: Context) = viewModelScope.launch {
        aboutSummary.update(context.getString(R.string.about_summary_short))
    }

    private suspend fun getBatteryOptimisation(context: Context): Flow<Boolean> = flow {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        emit(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    //Click methods
    fun onAccessibilityClicked(context: Context) = context.run {
        viewModelScope.launch {
            IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START).asFlow(this@run).collect {
                startActivity(Intent(context, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }
        }
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapAccessibilityService::class.java.name).flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
        }.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(
                this, null, NavOptions.Builder().withStandardAnimations().build(), null
            )
        }
    }

    fun onGestureAccessibilityClicked(context: Context) = context.run {
        viewModelScope.launch {
            IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START).asFlow(this@run).collect {
                startActivity(Intent(context, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }
        }
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapGestureAccessibilityService::class.java.name).flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
            putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
        }.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(
                this, null, NavOptions.Builder().withStandardAnimations().build(), null
            )
        }
    }

    fun onBatteryOptimisationClicked(context: Context) = context.run {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        }.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(this, null, null, null)
        }
    }

    fun onGestureClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsGestureFragment())
    }

    fun onActionsClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsActionFragment())
    }

    fun onActionsTripleClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsActionTripleFragment())
    }

    fun onGatesClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsGateFragment())
    }

    fun onFeedbackClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsFeedbackFragment())
    }

    fun onBackupRestoreClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsBackupRestoreFragment())
    }

    fun onAdvancedClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsAdvancedFragment())
    }

    fun onBatteryClicked(fragment: Fragment) = fragment.run {
        fragment.navigate(SettingsMainFragmentDirections.actionSettingsFragmentToModalActivity(R.navigation.nav_graph_modal_battery))
    }

    fun onDonateClicked(context: Context) = context.run {
        launchCCT(LINK_DONATE)
    }

    fun onTwitterClicked(context: Context) = context.run {
        launchCCT(LINK_TWITTER)
    }

    fun onAboutClicked(fragment: Fragment) = fragment.run {
        navigate(SettingsMainFragmentDirections.actionSettingsFragmentToSettingsAboutFragment())
    }

    enum class GestureAccessibilityServiceState {
        HIDDEN, NEEDS_ENABLE, NEEDS_DISABLE
    }

}