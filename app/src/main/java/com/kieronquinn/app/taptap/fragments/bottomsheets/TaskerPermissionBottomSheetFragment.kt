package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

class TaskerPermissionBottomSheetFragment : MaterialBottomSheetDialogFragment() {

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.bs_tasker_title)
            message(R.string.bs_tasker_content)
            positiveButton(R.string.bs_tasker_positive){
                try {
                    //Launch to Tasker settings
                    val intent = Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //This seems to set the current tab. Misc is the 4th tab.
                        putExtra("tno", 3)
                    }
                    it.context.startActivity(intent)
                }catch (e: Exception){
                    //Tasker not installed maybe?
                    e.printStackTrace()
                }
            }
            negativeButton(R.string.bs_tasker_negative)
        }
    }

}