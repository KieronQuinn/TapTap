package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.kieronquinn.app.taptap.R
import kotlinx.android.synthetic.main.bottom_sheet_buttons.*
import kotlinx.android.synthetic.main.fragment_bottomsheet_generic.*
import java.lang.Exception

class TaskerPermissionBottomSheetFragment : BottomSheetFragment() {

    init {
        layout = R.layout.fragment_bottomsheet_generic
        okLabel = R.string.bs_tasker_positive
        okListener = {
            try {
                //Launch to Tasker settings
                val intent = Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    //This seems to set the current tab. Misc is the 4th tab.
                    putExtra("tno", 3)
                }
                startActivity(intent)
            }catch (e: Exception){
                //Tasker not installed maybe?
            }
            true
        }
        cancelLabel = R.string.bs_tasker_negative
        cancelListener = {true}
        isCancelable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = getString(R.string.bs_tasker_content)
        bs_toolbar_title.text = getString(R.string.bs_tasker_title)
        text.text = message
    }

}