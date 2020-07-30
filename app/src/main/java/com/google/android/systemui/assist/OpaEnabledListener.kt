package com.google.android.systemui.assist

import android.content.Context

interface OpaEnabledListener {
    fun onOpaEnabledReceived(
        arg1: Context?,
        arg2: Boolean,
        arg3: Boolean,
        arg4: Boolean
    )
}