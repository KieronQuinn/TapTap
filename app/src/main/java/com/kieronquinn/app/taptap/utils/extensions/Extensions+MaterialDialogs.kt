package com.kieronquinn.app.taptap.utils.extensions

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updatePadding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.utils.MDUtil.dimenPx
import com.kieronquinn.app.taptap.R


fun MaterialDialog.applyTapTheme(): MaterialDialog {
    cornerRadius(res = R.dimen.bottom_sheet_corner_radius)
    onShow { dialog ->
        view.apply {
            findViewById<ViewGroup>(R.id.md_title_layout).apply {
                val mdTitleView = this.findViewById<TextView>(R.id.md_text_title)
                val containerWidth = measuredWidth - (2 * dimenPx(R.dimen.md_dialog_frame_margin_horizontal))
                mdTitleView.apply {
                    scrollBarSize = 0
                    post {
                        val newLeftMargin = (containerWidth / 2) - (measuredWidth / 2)
                        updatePadding(left = newLeftMargin)
                    }
                }
            }
        }
        window?.decorView?.findViewById<ViewGroup>(R.id.md_button_layout)?.apply {
            window?.navigationBarColor = (background as ColorDrawable).color
        }

    }
    return this
}