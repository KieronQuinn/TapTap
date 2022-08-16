package com.kieronquinn.app.taptap

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.res.ResourcesCompat
import com.google.android.columbus.*
import com.google.android.columbus.sensors.GestureController
import com.google.android.columbus.sensors.GestureSensor
import com.google.android.columbus.sensors.configuration.GestureConfiguration
import com.google.android.columbus.sensors.configuration.SensorConfiguration
import com.google.gson.Gson
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouterImpl
import com.kieronquinn.app.taptap.components.blur.BlurProvider
import com.kieronquinn.app.taptap.components.columbus.ColumbusServiceSettings
import com.kieronquinn.app.taptap.components.columbus.TapTapColumbusService
import com.kieronquinn.app.taptap.components.columbus.adjustments.SensitivityAdjustment
import com.kieronquinn.app.taptap.components.columbus.sensors.*
import com.kieronquinn.app.taptap.components.navigation.*
import com.kieronquinn.app.taptap.components.service.TapTapServiceRouter
import com.kieronquinn.app.taptap.components.service.TapTapServiceRouterImpl
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import com.kieronquinn.app.taptap.components.sui.SuiProvider
import com.kieronquinn.app.taptap.components.sui.SuiProviderImpl
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepositoryImpl
import com.kieronquinn.app.taptap.repositories.backuprestore.BackupRepository
import com.kieronquinn.app.taptap.repositories.backuprestore.BackupRepositoryImpl
import com.kieronquinn.app.taptap.repositories.backuprestore.RestoreRepository
import com.kieronquinn.app.taptap.repositories.backuprestore.RestoreRepositoryImpl
import com.kieronquinn.app.taptap.repositories.backuprestore.legacy.LegacyBackupRepository
import com.kieronquinn.app.taptap.repositories.backuprestore.legacy.LegacyBackupRepositoryImpl
import com.kieronquinn.app.taptap.repositories.crashreporting.CrashReportingRepository
import com.kieronquinn.app.taptap.repositories.crashreporting.CrashReportingRepositoryImpl
import com.kieronquinn.app.taptap.repositories.demomode.DemoModeRepository
import com.kieronquinn.app.taptap.repositories.demomode.DemoModeRepositoryImpl
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepositoryImpl
import com.kieronquinn.app.taptap.repositories.phonespecs.PhoneSpecsRepository
import com.kieronquinn.app.taptap.repositories.phonespecs.PhoneSpecsRepositoryImpl
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepositoryImpl
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabase
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabaseImpl
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepositoryImpl
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepositoryImpl
import com.kieronquinn.app.taptap.repositories.snapchat.SnapchatRepository
import com.kieronquinn.app.taptap.repositories.snapchat.SnapchatRepositoryImpl
import com.kieronquinn.app.taptap.repositories.update.UpdateRepository
import com.kieronquinn.app.taptap.repositories.update.UpdateRepositoryImpl
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDoubleImpl
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTripleImpl
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModel
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.decision.DecisionViewModel
import com.kieronquinn.app.taptap.ui.screens.decision.DecisionViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.disablecolumbus.DisableColumbusViewModel
import com.kieronquinn.app.taptap.ui.screens.disablecolumbus.DisableColumbusViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.reachability.ReachabilityViewModel
import com.kieronquinn.app.taptap.ui.screens.reachability.ReachabilityViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.root.RootSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.root.RootSharedViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.doubletap.SettingsActionsDoubleViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.doubletap.SettingsActionsDoubleViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddCategorySelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddCategorySelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions.SettingsActionsActionSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions.SettingsActionsActionSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.tripletap.SettingsActionsTripleViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.tripletap.SettingsActionsTripleViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModelDouble
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModelDoubleImpl
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModelTriple
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModelTripleImpl
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.SettingsAdvancedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.SettingsAdvancedViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity.SettingsAdvancedCustomSensitivityViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity.SettingsAdvancedCustomSensitivityViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.SettingsBackupRestoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.SettingsBackupRestoreViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup.SettingsBackupRestoreBackupViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.backup.SettingsBackupRestoreBackupViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.battery.SettingsBatteryViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.battery.SettingsBatteryViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.contributions.SettingsContributionsViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.contributions.SettingsContributionsViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.feedback.SettingsFeedbackViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.feedback.SettingsFeedbackViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddCategorySelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddCategorySelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates.SettingsGatesGateSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates.SettingsGatesGateSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.lowpower.SettingsLowPowerModeViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.lowpower.SettingsLowPowerModelViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.lowpower.shizukuinfo.SettingsLowPowerModeShizukuInfoViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.lowpower.shizukuinfo.SettingsLowPowerModeShizukuInfoViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.main.SettingsMainViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.main.SettingsMainViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.modelpicker.SettingsModelPickerViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.modelpicker.SettingsModelPickerViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.more.SettingsMoreViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.more.SettingsMoreViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.nativemode.SettingsNativeModeViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.nativemode.SettingsNativeModeViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.options.SettingsOptionsViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.options.SettingsOptionsViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.internet.SettingsSharedInternetPermissionDialogViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.internet.SettingsSharedInternetPermissionDialogViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts.SettingsSharedAppShortcutsSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts.SettingsSharedAppShortcutsSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting.SettingsSharedQuickSettingSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting.SettingsSharedQuickSettingSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts.SettingsSharedShortcutsSelectorViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts.SettingsSharedShortcutsSelectorViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat.SettingsSharedSnapchatViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat.SettingsSharedSnapchatViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.settings.update.SettingsUpdateViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.update.SettingsUpdateViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.complete.SetupCompleteViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.complete.SetupCompleteViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.SetupGestureViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.SetupGestureViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.configuration.SetupGestureConfigurationViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.configuration.SetupGestureConfigurationViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.info.SetupInfoViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.info.SetupInfoViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.landing.SetupLandingViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.landing.SetupLandingViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.notifications.SetupNotificationsViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.notifications.SetupNotificationsViewModelImpl
import com.kieronquinn.app.taptap.ui.screens.setup.upgrade.SetupUpgradeViewModel
import com.kieronquinn.app.taptap.ui.screens.setup.upgrade.SetupUpgradeViewModelImpl
import com.kieronquinn.app.taptap.utils.dummy.DummyStatusBarStateController
import com.kieronquinn.app.taptap.utils.dummy.DummyUiEventLogger
import com.kieronquinn.app.taptap.utils.lazy.LazyWrapper
import com.kieronquinn.app.taptap.utils.logging.UiEventLogger
import com.kieronquinn.app.taptap.utils.picasso.AppIconRequestHandler
import com.kieronquinn.app.taptap.utils.picasso.ComponentNameIconRequestHandler
import com.kieronquinn.app.taptap.utils.statusbar.StatusBarStateController
import com.kieronquinn.app.taptap.utils.wakefulness.WakefulnessLifecycle
import com.kieronquinn.monetcompat.core.MonetCompat
import com.squareup.picasso.Picasso
import com.topjohnwu.superuser.Shell
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.lsposed.hiddenapibypass.HiddenApiBypass

class TapTap : Application() {

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val singlesModule = module {
        single { BlurProvider.getBlurProvider(resources) }
        single { Gson() }
        single { createMarkwon() }
        single<TapTapSettings> { TapTapSettingsImpl(get(), get()) }
        single<TapTapAccessibilityRouter> { TapTapAccessibilityRouterImpl() }
        single<TapTapServiceRouter> { TapTapServiceRouterImpl() }
        single<SuiProvider>(createdAtStart = true) { SuiProviderImpl() }
    }

    private val repositoriesModule = module {
        single<TapTapDatabase> { TapTapDatabaseImpl(get(), get()) }
        single<ActionsRepository> { ActionsRepositoryImpl(get(), get(), get(), get()) }
        single<GatesRepository> { GatesRepositoryImpl(get()) }
        //When Gates are dependent on action IDs so need to be recreated
        factory<WhenGatesRepositoryDouble<*>> { WhenGatesRepositoryDoubleImpl(get(), get()) }
        factory<WhenGatesRepositoryTriple<*>> { WhenGatesRepositoryTripleImpl(get(), get()) }
        single<PhoneSpecsRepository> { PhoneSpecsRepositoryImpl(get(), get(), get()) }
        single<SnapchatRepository> { SnapchatRepositoryImpl(get(), get(), get()) }
        single<QuickSettingsRepository> { QuickSettingsRepositoryImpl(get()) }
        single<BackupRepository> { BackupRepositoryImpl(get(), get(), get(), get(), get(), get()) }
        single<RestoreRepository> { RestoreRepositoryImpl(get(), get(), get(), get(), get(), get(), get()) }
        single<LegacyBackupRepository> { LegacyBackupRepositoryImpl(get()) }
        single<TapTapRootServiceRepository> { TapTapRootServiceRepositoryImpl(get()) }
        single<UpdateRepository> { UpdateRepositoryImpl(get()) }
        single<DemoModeRepository> { DemoModeRepositoryImpl() }
        single<CrashReportingRepository> { CrashReportingRepositoryImpl(get()) }
    }

    private val viewModelModule = module {
        viewModel<RootSharedViewModel> { RootSharedViewModelImpl() }
        viewModel<DecisionViewModel> { DecisionViewModelImpl(get(), get(), get(), get()) }
        viewModel<SetupLandingViewModel> { SetupLandingViewModelImpl(get(), get()) }
        viewModel<ContainerSharedViewModel> { ContainerSharedViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<ContainerViewModel> { ContainerViewModelImpl(get(), get(), get(), get()) }
        viewModel<SettingsMainViewModel> { SettingsMainViewModelImpl(get(), get(), get(), get()) }
        viewModel<SettingsFeedbackViewModel> { SettingsFeedbackViewModelImpl(get()) }
        viewModel<SettingsOptionsViewModel> { SettingsOptionsViewModelImpl(get(), get(), get()) }
        viewModel<SettingsAdvancedViewModel> { SettingsAdvancedViewModelImpl(get(), get(), get()) }
        viewModel<SettingsLowPowerModeViewModel> { SettingsLowPowerModelViewModelImpl(get(), get(), get(), get()) }
        viewModel<SettingsAdvancedCustomSensitivityViewModel> { SettingsAdvancedCustomSensitivityViewModelImpl(get(), get(), get()) }
        viewModel<SettingsActionsDoubleViewModel> { SettingsActionsDoubleViewModelImpl(get(), get(), get(), get(), get(), get()) }
        viewModel<SettingsActionsTripleViewModel> { SettingsActionsTripleViewModelImpl(get(), get(), get(), get(), get(), get()) }
        viewModel<SettingsActionsAddCategorySelectorViewModel> { SettingsActionsAddCategorySelectorViewModelImpl(get(), get(), get())}
        viewModel<SettingsActionsActionSelectorViewModel> { SettingsActionsActionSelectorViewModelImpl(get(), get(), get()) }
        viewModel<SettingsSharedAppShortcutsSelectorViewModel> { SettingsSharedAppShortcutsSelectorViewModelImpl(get(), get()) }
        viewModel<SettingsSharedShortcutsSelectorViewModel> { SettingsSharedShortcutsSelectorViewModelImpl(get(), get())}
        viewModel<SettingsSharedShizukuPermissionFlowViewModel> { SettingsSharedShizukuPermissionFlowViewModelImpl(get()) }
        viewModel<SettingsSharedPackageSelectorViewModel> { SettingsSharedPackageSelectorViewModelImpl(get(), get()) }
        viewModel<SettingsLowPowerModeShizukuInfoViewModel> { SettingsLowPowerModeShizukuInfoViewModelImpl(get()) }
        viewModel<SettingsGatesViewModel> { SettingsGatesViewModelImpl(get(), get(), get(), get()) }
        viewModel<SettingsGatesGateSelectorViewModel> { SettingsGatesGateSelectorViewModelImpl(get(), get(), get()) }
        viewModel<SettingsGatesAddCategorySelectorViewModel> { SettingsGatesAddCategorySelectorViewModelImpl(get(), get(), get()) }
        viewModel<SettingsActionsWhenGatesViewModelDouble> { SettingsActionsWhenGatesViewModelDoubleImpl(get(), get(), get()) }
        viewModel<SettingsActionsWhenGatesViewModelTriple> { SettingsActionsWhenGatesViewModelTripleImpl(get(), get(), get()) }
        viewModel<SettingsModelPickerViewModel> { SettingsModelPickerViewModelImpl(get(), get()) }
        viewModel<SettingsBatteryViewModel> { SettingsBatteryViewModelImpl(get(), get(), get()) }
        viewModel<SettingsSharedSnapchatViewModel> { SettingsSharedSnapchatViewModelImpl(get(), get()) }
        viewModel<SettingsSharedQuickSettingSelectorViewModel> { SettingsSharedQuickSettingSelectorViewModelImpl(get(), get()) }
        viewModel<SettingsBackupRestoreViewModel> { SettingsBackupRestoreViewModelImpl(get()) }
        viewModel<SettingsBackupRestoreBackupViewModel> { SettingsBackupRestoreBackupViewModelImpl(get(), get(), get()) }
        viewModel<SettingsBackupRestoreRestoreViewModel> { SettingsBackupRestoreRestoreViewModelImpl(get(), get(), get()) }
        viewModel<SettingsMoreViewModel> { SettingsMoreViewModelImpl(get(), get(), get()) }
        viewModel<SettingsSharedInternetPermissionDialogViewModel> { SettingsSharedInternetPermissionDialogViewModelImpl(get()) }
        viewModel<SettingsContributionsViewModel> { SettingsContributionsViewModelImpl(get()) }
        viewModel<SettingsUpdateViewModel> { SettingsUpdateViewModelImpl(get(), get()) }
        viewModel<ReachabilityViewModel> { ReachabilityViewModelImpl(get(), get()) }
        viewModel<SetupGestureViewModel> { SetupGestureViewModelImpl(get(), get(), get(), get(), get()) }
        viewModel<SetupInfoViewModel> { SetupInfoViewModelImpl(get(), get()) }
        viewModel<SetupNotificationsViewModel> { SetupNotificationsViewModelImpl(get()) }
        viewModel<SetupGestureConfigurationViewModel> { SetupGestureConfigurationViewModelImpl(get(), get()) }
        viewModel<SetupCompleteViewModel> { SetupCompleteViewModelImpl(get(), get()) }
        viewModel<SetupUpgradeViewModel> { SetupUpgradeViewModelImpl(get(), get()) }
        viewModel<DisableColumbusViewModel> { DisableColumbusViewModelImpl(get(), get()) }
        viewModel<SettingsNativeModeViewModel> { SettingsNativeModeViewModelImpl(
            get(),
            get(),
            get(),
            get()
        ) }
    }

    private val navigationModule = module {
        single<RootNavigation> { RootNavigationImpl() }
        single<ContainerNavigation> { ContainerNavigationImpl() }
        single<GestureConfigurationNavigation> { GestureConfigurationNavigationImpl() }
    }

    private val columbusModule = module {
        scope<TapTapForegroundService> {
            scoped<TapTapShizukuServiceRepository> { TapTapShizukuServiceRepositoryImpl(get(), this, true) }
            scoped<StatusBarStateController> { DummyStatusBarStateController() }
            scoped<UiEventLogger> { DummyUiEventLogger() }
            scoped<ServiceEventEmitter> { ServiceEventEmitterImpl() }
            scoped { ColumbusServiceSettings() }
            scoped { SensorConfiguration(get()) }
            scoped { GestureConfiguration(listOf(SensitivityAdjustment(get(), get())), get()) }
            scoped { WakefulnessLifecycle() }
            scoped { ContentResolverWrapper(get()) }
            scoped { ColumbusContentObserverFactory(get(), mainHandler) }
            scoped { ColumbusSettings(get(), get()) }
            scoped { PowerManagerWrapper(this@TapTap) }
            scoped { createGestureSensor() }
            scoped { GestureController(get(), get()) }
            scoped { createColumbusService() }
            scoped { ColumbusServiceWrapper(get(), LazyWrapper { get() }) }
        }
    }

    private val appShortcutsModule = module {
        scope<SettingsSharedAppShortcutsSelectorViewModelImpl> {
            scoped<TapTapShizukuServiceRepository> { TapTapShizukuServiceRepositoryImpl(get(), this, false) }
        }
    }

    private val nativeModeModule = module {
        scope<SettingsNativeModeViewModelImpl> {
            scoped<TapTapShizukuServiceRepository> { TapTapShizukuServiceRepositoryImpl(get(), this, false) }
        }
    }

    private fun Scope.createGestureSensor(): GestureSensor {
        val columbusInitializer = get<ColumbusServiceSettings>()
        val demoModeRepository = get<DemoModeRepository>()
        val isTripleTapEnabled = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getTripleTapEnabled()
        }else columbusInitializer.isTripleTapEnabled
        val useContextHub = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getUseContextHub()
        }else columbusInitializer.useContextHub
        val useContextHubLogging = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getUseContextHub()
        }else columbusInitializer.useContextHubLogging
        return when {
            useContextHub -> {
                TapTapCHREGestureSensor(
                    get(),
                    columbusInitializer.lifecycleOwner,
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    mainHandler,
                    get(),
                    isTripleTapEnabled
                )
            }
            useContextHubLogging -> {
                TapTapCHRELogSensor(
                    this,
                    columbusInitializer.isTripleTapEnabled,
                    get(),
                    get()
                )
            }
            else -> {
                TapTapGestureSensorImpl(
                    this@TapTap,
                    mainHandler,
                    isTripleTapEnabled,
                    get(),
                    this,
                    columbusInitializer.tapModel,
                    get()
                )
            }
        }
    }

    private fun Scope.createColumbusService(): ColumbusService {
        val columbusInitializer = get<ColumbusServiceSettings>()
        val demoModeRepository = get<DemoModeRepository>()
        val actions = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getActions(this@TapTap, columbusInitializer.lifecycleOwner.lifecycle)
        }else columbusInitializer.actions
        val tripleTapActions = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getTripleActions(this@TapTap, columbusInitializer.lifecycleOwner.lifecycle)
        }else columbusInitializer.tripleTapActions
        val feedbackEffects = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getFeedbackEffects(this@TapTap, columbusInitializer.lifecycleOwner.lifecycle)
        }else columbusInitializer.feedbackEffects
        val gates = if(demoModeRepository.isDemoModeEnabled()){
            demoModeRepository.getGates(this@TapTap, columbusInitializer.lifecycleOwner.lifecycle)
        }else columbusInitializer.gates
        return TapTapColumbusService(
            actions,
            tripleTapActions,
            feedbackEffects.toSet(),
            gates.toSet(),
            get(),
            get(),
            this,
            get()
        )
    }

    private fun createMarkwon(): Markwon {
        val typeface = ResourcesCompat.getFont(this, R.font.google_sans_text_medium)
        return Markwon.builder(this).usePlugin(object: AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                typeface?.let {
                    builder.headingTypeface(it)
                    builder.headingBreakHeight(0)
                }
            }
        }).build()
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .addRequestHandler(ComponentNameIconRequestHandler(this))
                .build()
        )
        startKoin {
            androidContext(this@TapTap)
            modules(singlesModule, repositoriesModule, navigationModule, viewModelModule, appShortcutsModule, nativeModeModule, columbusModule)
        }
        setupCrashReporting()
        setupMonet()
    }

    private fun setupMonet(){
        val settings = get<TapTapSettings>()
        MonetCompat.enablePaletteCompat()
        MonetCompat.wallpaperColorPicker = {
            val selectedColor = settings.monetColor.getSync()
            if(selectedColor != Integer.MAX_VALUE && it?.contains(selectedColor) == true) selectedColor
            else it?.firstOrNull()
        }
    }

    private fun setupCrashReporting() {
        val settings = get<TapTapSettings>()
        val crashReporting = get<CrashReportingRepository>()
        crashReporting.setEnabled(settings.enableCrashReporting.getSync())
    }

}