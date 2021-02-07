package com.kieronquinn.app.taptap.ui.screens.setup.foss

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.isAccessibilityServiceEnabled
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.components.base.BaseFragment
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SetupFossInfoViewModel: BaseViewModel() {

    fun onFossInfoClicked(fragment: BaseFragment){
        fragment.navigate(SetupFossInfoFragmentDirections.actionSetupFossInfoFragmentToFossInfoBottomSheetDialogFragment())
    }

    fun onNextClicked(fragment: BaseFragment){
        if (isAccessibilityServiceEnabled(
                fragment.requireContext(),
                TapAccessibilityService::class.java
            )
        ) {
            fragment.navigate(SetupFossInfoFragmentDirections.actionSetupFossInfoFragmentToSetupBatteryFragment())
        } else {
            fragment.navigate(SetupFossInfoFragmentDirections.actionSetupFossInfoFragmentToSetupAccessibilityFragment())
        }
    }

    fun onLinkClicked(context: Context, url: String){
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }.run {
            context.startActivity(this)
        }
    }

}