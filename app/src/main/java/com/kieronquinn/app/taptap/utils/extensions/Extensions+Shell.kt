package com.kieronquinn.app.taptap.utils.extensions

import com.topjohnwu.superuser.Shell

fun Shell_isRooted(): Boolean {
    return Shell.cmd("whoami").exec().out.firstOrNull() == "root"
}