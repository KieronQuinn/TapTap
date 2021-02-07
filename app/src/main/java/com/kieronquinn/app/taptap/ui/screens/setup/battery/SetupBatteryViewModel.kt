package com.kieronquinn.app.taptap.ui.screens.setup.battery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.activities.ModalActivity
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*

class SetupBatteryViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    companion object {
        private val BATTERY_INTENT = Intent("android.settings.APP_BATTERY_SETTINGS").apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        private val dontKillMapping = mapOf(
            Pair("oneplus", "https://dontkillmyapp.com/oneplus"),
            Pair("huawei", "https://dontkillmyapp.com/huawei"),
            Pair("samsung", "https://dontkillmyapp.com/samsung"),
            Pair("xiaomi", "https://dontkillmyapp.com/xiaomi"),
            Pair("meizu", "https://dontkillmyapp.com/meizu"),
            Pair("asus", "https://dontkillmyapp.com/asus"),
            Pair("wiko", "https://dontkillmyapp.com/wiko"),
            Pair("lenovo", "https://dontkillmyapp.com/lenovo"),
            Pair("oppo", "https://dontkillmyapp.com/oppo"),
            Pair("nokia", "https://dontkillmyapp.com/nokia"),
            Pair("sony", "https://dontkillmyapp.com/sony")
        )
    }

    val shouldShowOemButton = MutableLiveData<Boolean>(false)
    val shouldShowMiuiButton = MutableLiveData<Boolean>(isMiui)
    val batteryOptimisationDisabled = MutableLiveData<Boolean>(false)
    val miuiOptimisationDisabled = MutableLiveData<Boolean>(false)

    val oemDontKillUrl = MediatorLiveData<String?>().apply {
        viewModelScope.launch {
            addSource(getDontKillUrl().asLiveData()){
                update(it)
            }
        }
    }

    val shouldShowDontKillButton = MediatorLiveData<Boolean>().apply {
        addSource(oemDontKillUrl){
            update(it?.isNotEmpty() == true)
        }
    }

    fun checkOem(context: Context) = viewModelScope.launch {
        getShowOemButton(context).collect {
            shouldShowOemButton.update(it)
        }
    }

    fun checkBatteryOptimisation(context: Context) = viewModelScope.launch {
        getBatteryOptimisation(context).collect {
            batteryOptimisationDisabled.update(it)
        }
    }

    fun checkMiuiOptimisation(context: Context) = viewModelScope.launch {
        getMiuiOptimisation(context).collect {
            miuiOptimisationDisabled.update(it ?: false)
        }
    }

    private suspend fun getBatteryOptimisation(context: Context): Flow<Boolean> = flow {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        emit(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    private suspend fun getShowOemButton(context: Context): Flow<Boolean> = flow {
        emit((context.packageManager.resolveActivity(BATTERY_INTENT, 0) != null) || AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(context))
    }

    private suspend fun getDontKillUrl(): Flow<String?> = flow {
        emit(dontKillMapping[Build.MANUFACTURER.toLowerCase(Locale.getDefault())])
    }

    private suspend fun getMiuiOptimisation(context: Context): Flow<Boolean?> {
        return Settings.Secure::class.java.getSettingAsFlow("miui_optimization", context.contentResolver, true)
    }

    fun onNextClicked(fragment: Fragment) {
        if(fragment.activity is ModalActivity) return
        tapSharedPreferences.hasSeenSetup = true
        //fragment.navigate(SetupBatteryFragmentDirections.actionSetupBatteryFragmentToSettingsActivity())
        //fragment.activity?.finish()
    }

    fun onBatteryButtonClicked(context: Context) {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        }.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(this, null, null, null)
        }
    }

    fun onMiuiBatteryButtonClicked(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).toActivityDestination(context).run {
            ActivityNavigator(context).navigate(this, null, NavOptions.Builder().withStandardAnimations().build(), null)
            Toast.makeText(context, context.getString(R.string.setup_battery_button_miui_toast), Toast.LENGTH_LONG).show()
        }
    }

    fun onDontKillButtonClicked(context: Context) {
        context.launchCCT(oemDontKillUrl.value!!)
    }

    fun onOemBatteryButtonClicked(context: Context) {
        if(context.packageManager.resolveActivity(BATTERY_INTENT, 0) != null){
            startOemBatterySettings(context)
        }else{
            runCatching {
                AutoStartPermissionHelper.getInstance().getAutoStartPermission(context)
            }.onFailure {
                Toast.makeText(context, context.getString(R.string.setup_battery_button_oem_toast), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startOemBatterySettings(context: Context){
        BATTERY_INTENT.toActivityDestination(context).run {
            ActivityNavigator(context).navigate(this, null, NavOptions.Builder().withStandardAnimations().build(), null)
        }
    }

    override fun onBackPressed(fragment: Fragment): Boolean {
        return if(fragment.activity is ModalActivity){
            fragment.activity?.finish()
            true
        }else super.onBackPressed(fragment)
    }

}