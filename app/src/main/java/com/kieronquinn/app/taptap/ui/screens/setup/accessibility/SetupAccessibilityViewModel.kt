package com.kieronquinn.app.taptap.ui.screens.setup.accessibility

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SetupAccessibilityViewModel: BaseViewModel() {

    companion object {
        private val ACCESSIBILITY_SERVICE_COMPONENT = ComponentName(BuildConfig.APPLICATION_ID, TapAccessibilityService::class.java.name)
    }

    val isAccessibilityServiceEnabled = MutableLiveData<Boolean>()

    fun setupAccessibilityListener(context: Context) = viewModelScope.launch {
        getAccessibilityListener(context).collect {
            isAccessibilityServiceEnabled.update(it?.doesContainComponentName(ACCESSIBILITY_SERVICE_COMPONENT) ?: false)
        }
    }

    fun setupAccessibilityLaunchListener(activity: Activity) = viewModelScope.launch {
        getAccessibilityStartListener(activity).collect {
            activity.startActivity(activity.intent.apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    private suspend fun getAccessibilityListener(context: Context): Flow<String?> {
        return Settings.Secure::class.java.getSettingAsFlow(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, context.contentResolver, true)
    }

    private suspend fun getAccessibilityStartListener(activity: Activity): Flow<Intent?> {
        return IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START).asFlow(activity)
    }

    fun onAccessibilityClicked(context: Context){
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

    fun onNextClicked(fragment: Fragment){
        fragment.navigate(SetupAccessibilityFragmentDirections.actionSetupAccessibilityFragmentToSetupBatteryFragment())
    }

}