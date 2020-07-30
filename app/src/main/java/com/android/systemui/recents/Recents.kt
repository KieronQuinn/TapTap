package com.android.systemui.recents

import com.kieronquinn.app.taptap.utils.getDependency
import de.robv.android.xposed.XposedHelpers

class Recents(classLoader: ClassLoader) {

    private val recentsClass = XposedHelpers.findClass("com.android.systemui.recents.Recents", classLoader)
    private val recentsInstance = classLoader.getDependency(recentsClass)

    fun toggleRecentApps(){
        recentsClass.getMethod("toggleRecentApps").invoke(recentsInstance)
    }

}