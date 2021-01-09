package com.kieronquinn.app.taptap

import android.app.Application
import android.content.Context
import com.google.android.systemui.columbus.ColumbusContentObserver
import com.google.android.systemui.columbus.ContentResolverWrapper
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.config.Adjustment
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.core.TapGestureSensorImpl
import com.kieronquinn.app.taptap.core.TapColumbusService
import com.kieronquinn.app.taptap.core.TapServiceContainer
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.AppIconRequestHandler
import com.kieronquinn.app.taptap.utils.activityManagerNative
import com.kieronquinn.app.taptap.v2.ui.screens.modal.ModalNoGyroscopeViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.accessibility.SetupAccessibilityViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.battery.SetupBatteryViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.configuration.SetupConfigurationViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.configuration.preference.SetupConfigurationPreferenceViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.landing.SetupLandingViewModel
import com.kieronquinn.app.taptap.v2.ui.screens.setup.foss.SetupFossInfoViewModel
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TapTapApplication : Application() {

    var disableWake = false

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
                single<GestureSensor> { TapGestureSensorImpl(get(), get(), createGestureConfiguration(get(), activityManagerNative)) }
                single { PowerManagerWrapper(get()) }
                //Tap Service
                single { TapColumbusService(get(), get(), get(), get()) }
                single { TapServiceContainer() }
                //Setup flow
                viewModel { SetupLandingViewModel(get()) }
                viewModel { SetupConfigurationViewModel(get()) }
                viewModel { SetupConfigurationPreferenceViewModel(get()) }
                viewModel { SetupFossInfoViewModel() }
                viewModel { SetupAccessibilityViewModel() }
                viewModel { SetupBatteryViewModel(get()) }
                //No gyroscope modal
                viewModel { ModalNoGyroscopeViewModel() }
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