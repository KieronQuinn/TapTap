package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService

class KeyboardVisibility(context: Context) : Gate(context) {

    private val keyboardPackageName by lazy {
        val default = "com.google.android.inputmethod.latin"
        val component = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val componentName = ComponentName.unflattenFromString(component)
        componentName?.packageName ?: default
    }

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val tapAccessibilityService = context as TapAccessibilityService
        return tapAccessibilityService.getCurrentPackageName() == keyboardPackageName
    }

}