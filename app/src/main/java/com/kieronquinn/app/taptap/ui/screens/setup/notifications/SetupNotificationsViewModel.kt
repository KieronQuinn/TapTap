package com.kieronquinn.app.taptap.ui.screens.setup.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import com.kieronquinn.app.taptap.utils.extensions.hasNotificationPermission
import kotlinx.coroutines.launch

abstract class SetupNotificationsViewModel: BaseSetupViewModel() {

    abstract fun checkPermission(context: Context)
    abstract fun onPermissionResult(context: Context, granted: Boolean)
    abstract fun onGrantClicked(launcher: ActivityResultLauncher<String>)

}

@SuppressLint("InlinedApi")
class SetupNotificationsViewModelImpl(
    private val navigation: RootNavigation
): SetupNotificationsViewModel() {

    override fun checkPermission(context: Context) {
        if(context.hasNotificationPermission()){
            viewModelScope.launch {
                navigation.navigate(SetupNotificationsFragmentDirections.actionSetupNotificationsFragmentToSetupGestureFragment())
            }
        }
    }

    override fun onPermissionResult(context: Context, granted: Boolean) {
        if(granted){
            checkPermission(context)
        }else{
            viewModelScope.launch {
                navigation.navigate(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                })
            }
        }
    }

    override fun onGrantClicked(launcher: ActivityResultLauncher<String>) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onBackPressed(): Boolean {
        viewModelScope.launch {
            navigation.navigateBack()
        }
        return true
    }

}