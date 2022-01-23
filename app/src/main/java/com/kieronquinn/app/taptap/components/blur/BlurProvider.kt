package com.kieronquinn.app.taptap.components.blur

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

abstract class BlurProvider {

    companion object {
        private fun lerp(start: Float, stop: Float, amount: Float): Float {
            return start + (stop - start) * amount
        }

        fun getBlurProvider(resources: Resources): BlurProvider {
            return when {
                Build.VERSION.SDK_INT >= 31 -> BlurProvider31(resources)
                Build.VERSION.SDK_INT >= 30 -> BlurProvider30(resources)
                else -> BlurProvider29()
            }
        }
    }

    abstract val minBlurRadius: Float
    abstract val maxBlurRadius: Float

    abstract fun applyDialogBlur(dialogWindow: Window, appWindow: Window, ratio: Float)
    abstract fun applyBlurToWindow(window: Window, ratio: Float)

    internal fun Window.clearDimming() {
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    internal fun Window.addDimming() {
        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    internal fun blurRadiusOfRatio(ratio: Float): Int {
        return if (ratio == 0.0f) 0 else lerp(minBlurRadius, maxBlurRadius, ratio).toInt()
    }

}