package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kieronquinn.app.taptap.R

fun View.onApplyInsets(block: (view: View, insets: WindowInsetsCompat) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        block(view, insets)
        insets
    }
}

fun View.applyBottomInsets(rootView: View, extraBottomPadding: Int = 0) {
    val legacyWorkaround = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        context.getLegacyWorkaroundNavBarHeight()
    } else 0
    val bottomPadding = resources.getDimension(R.dimen.margin_16)
        .toInt() + resources.getDimension(R.dimen.bottom_navigation_height)
        .toInt() + extraBottomPadding + legacyWorkaround
    rootView.onApplyInsets { _, insets ->
        updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom + bottomPadding)
    }
    updatePadding(bottom = bottomPadding)
}

private fun Context.getLegacyWorkaroundNavBarHeight(): Int {
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}