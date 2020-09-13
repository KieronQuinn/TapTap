package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.UpdateChecker
import com.kieronquinn.app.taptap.utils.formatChangelog
import com.kieronquinn.app.taptap.utils.getSpannedText
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemGestureInsetsToMargin
import kotlinx.android.synthetic.main.bottom_sheet_buttons.*
import kotlinx.android.synthetic.main.fragment_bottomsheet_generic.*
import java.lang.Exception
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class UpdateBottomSheetFragment : BottomSheetFragment() {

    companion object {
        const val KEY_UPDATE = "update"
    }

    init {
        layout = R.layout.fragment_bottomsheet_generic
        okLabel = R.string.bs_update_download
        okListener = {
            val update = requireArguments().getParcelable<UpdateChecker.Update>(KEY_UPDATE)!!
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(update.assetUrl)))
            true
        }
        cancelLabel = R.string.bs_update_later
        cancelListener = {true}
        isCancelable = true
        isSwipeable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val update = requireArguments().getParcelable<UpdateChecker.Update>(KEY_UPDATE)!!
        val timestamp = DateFormat.getDateFormat(view.context).format(Date.from(Instant.parse(update.timestamp)))
        val message = getSpannedText(getString(R.string.bs_update_content, BuildConfig.VERSION_NAME, update.name, timestamp, update.changelog.formatChangelog()))
        bs_toolbar_title.text = getString(R.string.bs_update_title)
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