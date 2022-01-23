package com.kieronquinn.app.taptap.components.blur

import android.view.Window

class BlurProvider29: BlurProvider() {

    override val minBlurRadius = 0f
    override val maxBlurRadius = 0f

    override fun applyDialogBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        applyBlurToWindow(dialogWindow, ratio)
    }

    override fun applyBlurToWindow(window: Window, ratio: Float) {
        window.addDimming()
    }

}