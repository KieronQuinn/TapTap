package com.kieronquinn.app.taptap.utils.extensions

import android.util.LayoutDirection
import androidx.core.text.layoutDirection
import java.util.Locale

fun isRtl() = Locale.getDefault().layoutDirection == LayoutDirection.RTL