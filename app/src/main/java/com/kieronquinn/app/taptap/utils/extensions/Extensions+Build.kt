package com.kieronquinn.app.taptap.utils.extensions

import android.os.Build

private val isPreviewSdk by lazy {
    Build.VERSION.PREVIEW_SDK_INT > 0
}

fun Build_isAtLeastT(): Boolean {
    return Build.VERSION.SDK_INT >= 33 || (isPreviewSdk && Build.VERSION.SDK_INT == 32)
}