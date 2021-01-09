package com.kieronquinn.app.taptap.v2.ui.screens.setup.foss

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.utils.isAccessibilityServiceEnabled
import com.kieronquinn.app.taptap.utils.navigate
import com.kieronquinn.app.taptap.v2.components.base.BaseFragment
import com.kieronquinn.app.taptap.v2.components.base.BaseViewModel

class SetupFossInfoViewModel: BaseViewModel() {

    fun onFossInfoClicked(fragment: BaseFragment){
        fragment.navigate(SetupFossInfoFragmentDirections.actionSetupFossInfoFragmentToFossInfoBottomSheetDialogFragment())
    }

    fun onNextClicked(fragment: BaseFragment){
        if (isAccessibilityServiceEnabled(fragment.requireContext(), TapAccessibilityService::class.java)) {
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