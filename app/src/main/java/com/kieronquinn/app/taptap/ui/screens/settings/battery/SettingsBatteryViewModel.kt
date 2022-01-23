package com.kieronquinn.app.taptap.ui.screens.settings.battery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsBatteryViewModel : GenericSettingsViewModel() {

    abstract val showHeader: StateFlow<Boolean>
    abstract val batteryOptimisationSetting: TapTapSettings.TapTapSetting<Boolean>
    abstract val batteryOptimisationDisabled: StateFlow<Boolean>
    abstract val oemBatteryAvailable: Boolean
    abstract fun onResume()
    abstract fun onDontKillLinkClicked(ignored: String)
    abstract fun onOemClicked(context: Context)
    abstract fun onHeaderLinkClicked(url: String)
    abstract fun onHeaderDismissed()

}

class SettingsBatteryViewModelImpl(
    context: Context,
    private val navigation: ContainerNavigation,
    settings: TapTapSettings
) : SettingsBatteryViewModel() {

    companion object {
        private const val DONT_KILL_ROOT = "https://dontkillmyapp.com/"
        private val DONT_KILL_MAPPING = mapOf(
            Pair("oneplus", "https://dontkillmyapp.com/oneplus"),
            Pair("huawei", "https://dontkillmyapp.com/huawei"),
            Pair("samsung", "https://dontkillmyapp.com/samsung"),
            Pair("xiaomi", "https://dontkillmyapp.com/xiaomi"),
            Pair("meizu", "https://dontkillmyapp.com/meizu"),
            Pair("asus", "https://dontkillmyapp.com/asus"),
            Pair("wiko", "https://dontkillmyapp.com/wiko"),
            Pair("lenovo", "https://dontkillmyapp.com/lenovo"),
            Pair("oppo", "https://dontkillmyapp.com/oppo"),
            Pair("vivo", "https://dontkillmyapp.com/vivo"),
            Pair("realme", "https://dontkillmyapp.com/realme"),
            Pair("blackview", "https://dontkillmyapp.com/blackview"),
            Pair("unihertz", "https://dontkillmyapp.com/unihertz"),
            Pair("nokia", "https://dontkillmyapp.com/hmd-global"),
            Pair("sony", "https://dontkillmyapp.com/sony")
        )
    }

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val resumeBus = MutableSharedFlow<Unit>()
    private val autoStarter = AutoStartPermissionHelper.getInstance()
    private val batteryShowWarning = settings.batteryShowWarning

    override val showHeader = batteryShowWarning.asFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, batteryShowWarning.getSync())

    private val isIgnoringOptimisations: Boolean
        get() = powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)

    override val oemBatteryAvailable = autoStarter.isAutoStartPermissionAvailable(context)

    override val batteryOptimisationDisabled = resumeBus.map {
        isIgnoringOptimisations
    }.stateIn(viewModelScope, SharingStarted.Eagerly, isIgnoringOptimisations)

    override val batteryOptimisationSetting = TapTapSettings.FakeTapTapSetting(
        batteryOptimisationDisabled,
        ::onBatteryOptimisationClicked
    )

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(Unit)
        }
    }

    private fun onBatteryOptimisationClicked(ignored: Boolean) {
        viewModelScope.launch {
            navigation.navigate(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            })
        }
    }

    override fun onOemClicked(context: Context) {
        autoStarter.getAutoStartPermission(context, open = true, newTask = true)
    }

    override fun onDontKillLinkClicked(ignored: String) {
        viewModelScope.launch {
            val url = DONT_KILL_MAPPING[Build.MANUFACTURER.lowercase()] ?: DONT_KILL_ROOT
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

    override fun onHeaderLinkClicked(url: String) {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

    override fun onHeaderDismissed() {
        viewModelScope.launch {
            batteryShowWarning.set(false)
        }
    }

}