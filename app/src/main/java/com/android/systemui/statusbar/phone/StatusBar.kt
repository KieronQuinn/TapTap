package com.android.systemui.statusbar.phone

import com.kieronquinn.app.taptap.utils.getDependency
import de.robv.android.xposed.XposedHelpers

class StatusBar(private val actualStatusBar: Any, classLoader: ClassLoader) {

    private val statusBarClass = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBar", classLoader)

    fun collapseShade(){
        statusBarClass.getMethod("collapseShade").invoke(actualStatusBar)
    }

}