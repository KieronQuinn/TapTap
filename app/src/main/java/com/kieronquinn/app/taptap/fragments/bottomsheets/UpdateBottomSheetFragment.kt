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
import com.afollestad.materialdialogs.MaterialDialog
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

class UpdateBottomSheetFragment : MaterialBottomSheetDialogFragment() {

    companion object {
        const val KEY_UPDATE = "update"
    }

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.bs_update_title)
            val update = requireArguments().getParcelable<UpdateChecker.Update>(KEY_UPDATE)!!
            val timestamp = DateFormat.getDateFormat(view.context).format(Date.from(Instant.parse(update.timestamp)))
            val message = getSpannedText(getString(R.string.bs_update_content, BuildConfig.VERSION_NAME, update.name, timestamp, update.changelog.formatChangelog()))
            message(text = message)
            positiveButton(R.string.bs_update_download){
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(update.assetUrl)))
            }
            neutralButton(R.string.configuration_button_close)
        }
    }

}