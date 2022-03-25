package com.kieronquinn.app.taptap.utils.extensions

import android.Manifest
import com.kieronquinn.app.taptap.BuildConfig

const val EXTRA_KEY_IS_FROM_COLUMBUS = "systemui_google_quick_tap_is_source"

fun execGrantReadLogsPermission() {
    Runtime.getRuntime().exec("pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.READ_LOGS}")
}