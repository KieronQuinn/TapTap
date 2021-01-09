package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.EXTRA_FRAGMENT_ARG_KEY
import com.kieronquinn.app.taptap.utils.EXTRA_SHOW_FRAGMENT_ARGUMENTS
import java.lang.Exception

class SecondaryServiceBottomSheetFragment : MaterialBottomSheetDialogFragment() {

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.bs_secondary_service_title)
            message(R.string.bs_secondary_service_content)
            positiveButton(R.string.notification_policy_grant){
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        val bundle = Bundle()
                        val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapGestureAccessibilityService::class.java.name).flattenToString()
                        bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
                        putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                        putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
                    })
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    Toast.makeText(it.context, R.string.accessibility_info_toast_gesture, Toast.LENGTH_LONG).show()
                }catch (e: Exception){
                }
            }
            negativeButton(android.R.string.cancel)
        }
    }

}