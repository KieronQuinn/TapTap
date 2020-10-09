package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemGestureInsetsToMargin
import kotlinx.android.synthetic.main.bottom_sheet_buttons.*
import kotlinx.android.synthetic.main.fragment_bottomsheet_generic.*
import java.lang.Exception

class NotificationPolicyBottomSheetFragment : MaterialBottomSheetDialogFragment() {

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.bs_tasker_title)
            message(R.string.bs_notification_policy_content)
            positiveButton(R.string.notification_policy_grant){
                try {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }catch (e: Exception){
                }
            }
            negativeButton(android.R.string.cancel)
        }
    }

}