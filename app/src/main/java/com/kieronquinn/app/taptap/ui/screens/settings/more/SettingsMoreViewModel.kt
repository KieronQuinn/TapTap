package com.kieronquinn.app.taptap.ui.screens.settings.more

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.crashreporting.CrashReportingRepository
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsMoreViewModel: GenericSettingsViewModel() {

    abstract val internetAccessEnabled: TapTapSettings.TapTapSetting<Boolean>
    abstract val crashReportingEnabled: TapTapSettings.TapTapSetting<Boolean>

    abstract fun onBatteryClicked()
    abstract fun onBackupRestoreClicked()
    abstract fun onAboutContributorsClicked()
    abstract fun onAboutDonateClicked()
    abstract fun onAboutGitHubClicked()
    abstract fun onAboutTwitterClicked()
    abstract fun onAboutXDAClicked()
    abstract fun onAboutLibrariesClicked()

}

class SettingsMoreViewModelImpl(
    private val navigation: ContainerNavigation,
    settings: TapTapSettings,
    crashReporting: CrashReportingRepository
): SettingsMoreViewModel() {

    private val internetAllowed = settings.internetAllowed
    private val backgroundUpdateCheck = settings.backgroundUpdateCheck

    companion object {
        private const val LINK_TWITTER = "https://kieronquinn.co.uk/redirect/TapTap/twitter"
        private const val LINK_GITHUB = "https://kieronquinn.co.uk/redirect/TapTap/github"
        private const val LINK_XDA = "https://kieronquinn.co.uk/redirect/TapTap/xda"
        private const val LINK_DONATE = "https://kieronquinn.co.uk/redirect/TapTap/donate"
    }

    private val internetAccessStateFlow = internetAllowed.asFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, internetAllowed.getSync())

    override val internetAccessEnabled = TapTapSettings.FakeTapTapSetting(internetAccessStateFlow){
        viewModelScope.launch {
            if(it){
                //Show the dialog for options
                navigation.navigate(SettingsMoreFragmentDirections.actionGlobalSettingsSharedInternetPermissionDialogFragment())
            }else{
                //Disable settings
                internetAllowed.set(false)
                backgroundUpdateCheck.set(false)
            }
        }
    }

    override val crashReportingEnabled = settings.enableCrashReporting.apply {
        viewModelScope.launch {
            asFlow().collect {
                crashReporting.setEnabled(it)
            }
        }
    }

    override fun onBatteryClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsMoreFragmentDirections.actionSettingsMoreFragmentToSettingsBatteryFragment())
        }
    }

    override fun onBackupRestoreClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsMoreFragmentDirections.actionSettingsMoreFragmentToSettingsBackupRestoreFragment())
        }
    }

    override fun onAboutContributorsClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsMoreFragmentDirections.actionSettingsMoreFragmentToSettingsContributionsFragment())
        }
    }

    override fun onAboutDonateClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(LINK_DONATE)
            })
        }
    }

    override fun onAboutGitHubClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(LINK_GITHUB)
            })
        }
    }

    override fun onAboutLibrariesClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsMoreFragmentDirections.actionSettingsMoreFragmentToOssLicensesMenuActivity())
        }
    }

    override fun onAboutTwitterClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(LINK_TWITTER)
            })
        }
    }

    override fun onAboutXDAClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(LINK_XDA)
            })
        }
    }

}