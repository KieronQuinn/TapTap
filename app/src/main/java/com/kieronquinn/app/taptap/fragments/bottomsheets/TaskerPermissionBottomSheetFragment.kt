package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.kieronquinn.app.taptap.R
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemGestureInsetsToMargin
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
        view.applySystemGestureInsetsToMargin(bottom = true)
    }

    //Hacks to make the bottom sheet draw below the nav bar
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.findViewById<View>(com.google.android.material.R.id.container).fitsSystemWindows = false
                window.decorView.run {
                    //systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    Insetter.setEdgeToEdgeSystemUiFlags(this, true)
                }
            }
            //Fix the sheet drawing behind the status bar
            window.findViewById<View>(com.google.android.material.R.id.coordinator).setOnApplyWindowInsetsListener { v, insets ->
                v.layoutParams.apply {
                    this as FrameLayout.LayoutParams
                    topMargin = insets.systemWindowInsetTop
                }
                insets
            }
        }

    }

}