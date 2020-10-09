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
import com.kieronquinn.app.taptap.activities.ModalActivity
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

class BatteryOptimisationBottomSheetFragment : MaterialBottomSheetDialogFragment() {

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.battery_and_optimisation)
            message(R.string.bs_battery_content)
            positiveButton(android.R.string.ok)
            negativeButton(R.string.bs_battery_neutral){
                startActivity(Intent(context, ModalActivity::class.java).apply {
                    putExtra(ModalActivity.KEY_NAV_GRAPH, R.navigation.nav_graph_modal_battery)
                })
            }
        }
    }

}