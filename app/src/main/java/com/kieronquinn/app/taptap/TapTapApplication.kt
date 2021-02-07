package com.kieronquinn.app.taptap

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.google.android.systemui.columbus.ColumbusContentObserver
import com.google.android.systemui.columbus.ContentResolverWrapper
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.config.Adjustment
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.core.*
import com.kieronquinn.app.taptap.utils.AppIconRequestHandler
import com.kieronquinn.app.taptap.utils.UpdateChecker
import com.kieronquinn.app.taptap.utils.extensions.activityManagerNative
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModel
import com.kieronquinn.app.taptap.ui.screens.modal.ModalNoGyroscopeViewModel
import com.kieronquinn.app.taptap.ui.screens.picker.app.AppPickerViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.about.SettingsAboutViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.category.SettingsActionAddCategoryViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.list.SettingsActionAddListViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.double.SettingsDoubleTapActionViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.triple.SettingsTripleTapActionViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.SettingsAdvancedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity.SettingsAdvancedCustomSensitivityBottomSheetDialogViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.SettingsBackupRestoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup.SettingsBackupRestoreBackupViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.SettingsGateViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.category.SettingsGateAddCategoryViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.list.SettingsGateAddListViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gesture.SettingsGestureViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gesture.model.SettingsGestureModelBottomSheetViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.main.SettingsMainViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.accessibility.SetupAccessibilityViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.battery.SetupBatteryViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.configuration.SetupConfigurationViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference.SetupConfigurationPreferenceViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.configuration.preference.modelpicker.SetupConfigurationPreferenceModelPickerBottomSheetViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.foss.SetupFossInfoViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.landing.SetupLandingViewModel
import com.kieronquinn.app.taptap.ui.screens.update.download.UpdateDownloadBottomSheetViewModel
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TapTapApplication : Application() {

    var disableWake = false
    var requireUnlock = false

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .build())
        startKoin {
            androidContext(this@TapTapApplication)
            val modules = module {
                //Tap Service Dependencies
                single { TapSharedPreferences(get()) }
                single<GestureSensor> { TapGestureSensorImpl(get(), get(), createGestureConfiguration(get(),
                    activityManagerNative
                )) }
                single { PowerManagerWrapper(get()) }
                single { TapFileRepository(get(), get()) }
                //Tap Service
                single { TapColumbusService(get(), get(), get(), get(), get()) }
                single { TapServiceContainer() }
                //Container fragment
                viewModel { ContainerViewModel(get()) }
                //Setup flow
                viewModel { SetupLandingViewModel(get()) }
                viewModel { SetupConfigurationViewModel(get()) }
                viewModel { SetupConfigurationPreferenceViewModel(get()) }
                viewModel { SetupConfigurationPreferenceModelPickerBottomSheetViewModel(get()) }
                viewModel { SetupFossInfoViewModel() }
                viewModel { SetupAccessibilityViewModel() }
                viewModel { SetupBatteryViewModel(get()) }
                //No gyroscope modal
                viewModel { ModalNoGyroscopeViewModel() }
                //Settings
                viewModel { SettingsMainViewModel(get()) }
                //Settings > Gesture
                viewModel { SettingsGestureViewModel(get()) }
                viewModel { SettingsGestureModelBottomSheetViewModel(get()) }
                //Settings > Action
                viewModel { SettingsDoubleTapActionViewModel(get(), get()) }
                viewModel { SettingsTripleTapActionViewModel(get(), get()) }
                //Settings > Action > Add
                viewModel { SettingsActionAddContainerBottomSheetViewModel() }
                viewModel { SettingsActionAddCategoryViewModel() }
                viewModel { SettingsActionAddListViewModel() }
                //Settings > Gate
                viewModel { SettingsGateViewModel(get()) }
                //Settings > Gate > Add
                viewModel { SettingsGateAddContainerBottomSheetViewModel() }
                viewModel { SettingsGateAddCategoryViewModel() }
                viewModel { SettingsGateAddListViewModel(get()) }
                //Settings > Advanced
                viewModel { SettingsAdvancedViewModel() }
                viewModel { SettingsAdvancedCustomSensitivityBottomSheetDialogViewModel(get()) }
                //Settings > Backup & Restore
                viewModel { SettingsBackupRestoreViewModel(get()) }
                viewModel { SettingsBackupRestoreBackupViewModel(get()) }
                viewModel { SettingsBackupRestoreRestoreViewModel(get()) }
                //App Picker
                viewModel { AppPickerViewModel() }
                //About
                viewModel { SettingsAboutViewModel() }
                //Updater
                viewModel { UpdateDownloadBottomSheetViewModel(getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager) }
                //Update module
                single { UpdateChecker() }
            }
            modules(modules)
        }
    }

    private fun createGestureConfiguration(context: Context, activityManager: Any): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first()
            .newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet<Adjustment>(), factory)
    }

}