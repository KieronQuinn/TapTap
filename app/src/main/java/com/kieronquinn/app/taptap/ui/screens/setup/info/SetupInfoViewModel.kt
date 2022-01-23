package com.kieronquinn.app.taptap.ui.screens.setup.info

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.databinding.FragmentSetupInfoBinding
import com.kieronquinn.app.taptap.ui.screens.decision.DecisionFragmentDirections
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import kotlinx.coroutines.launch

abstract class SetupInfoViewModel: BaseSetupViewModel() {

    abstract fun onLinkClicked(url: String)
    abstract fun onSourceClicked()
    abstract fun onNextClicked()

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

    override fun onNextClicked() {
        viewModelScope.launch {
            navigation.navigate(SetupInfoFragmentDirections.actionSetupInfoFragmentToSetupGestureFragment())
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