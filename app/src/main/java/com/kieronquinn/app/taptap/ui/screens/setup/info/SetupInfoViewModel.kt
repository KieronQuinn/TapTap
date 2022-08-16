package com.kieronquinn.app.taptap.ui.screens.setup.info

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.decision.DecisionFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import com.kieronquinn.app.taptap.utils.extensions.hasNotificationPermission
import kotlinx.coroutines.launch

abstract class SetupInfoViewModel: BaseSetupViewModel() {

    abstract fun onLinkClicked(url: String)
    abstract fun onSourceClicked()
    abstract fun onNextClicked(context: Context)

}

class SetupInfoViewModelImpl(private val settings: TapTapSettings, private val navigation: RootNavigation): SetupInfoViewModel() {

    override fun onLinkClicked(url: String) {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

    override fun onSourceClicked() {
        viewModelScope.launch {
            navigation.navigate(SetupInfoFragmentDirections.actionSetupInfoFragmentToSetupInfoWarningBottomSheetFragment())
        }
    }

    override fun onNextClicked(context: Context) {
        viewModelScope.launch {
            if(context.hasNotificationPermission()) {
                navigation.navigate(SetupInfoFragmentDirections.actionSetupInfoFragmentToSetupGestureFragment())
            }else{
                navigation.navigate(SetupInfoFragmentDirections.actionSetupInfoFragmentToSetupNotificationsFragment())
            }
            if(!settings.internetAllowed.exists()) {
                navigation.navigate(DecisionFragmentDirections.actionGlobalSharedInternetPermissionDialogFragment())
            }
        }
    }

    override fun onBackPressed(): Boolean {
        viewModelScope.launch {
            navigation.navigateBack()
        }
        return true
    }

}