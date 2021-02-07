package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.preference

import androidx.preference.PreferenceScreen
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.components.base.BaseSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.SettingsBackupRestoreFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.SettingsBackupRestoreViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsBackupRestorePreferenceFragment: BaseSettingsFragment(R.xml.settings_backup_restore) {

    private val viewModel by sharedViewModel<SettingsBackupRestoreViewModel>()

    override val disableInsetAdjustment = true
    override val disableToolbarBackground: Boolean = true

    private val backupPreference by lazy {
        findPreference<Preference>("backup")
    }

    private val restorePreference by lazy {
        findPreference<Preference>("restore")
    }

    override fun setupPreferences(preferenceScreen: PreferenceScreen) {
        with(viewModel){
            backupPreference.bindOnClick(::onBackupClicked, parentFragment as SettingsBackupRestoreFragment)
            restorePreference.bindOnClick(::onRestoreClicked, parentFragment as SettingsBackupRestoreFragment)
        }
    }

}