package com.kieronquinn.app.taptap.components.blur

import android.app.ActivityManagerHidden
import android.content.res.Resources
import android.os.Build
import android.os.SystemProperties
import android.view.SurfaceControl
import android.view.Window
import androidx.annotation.RequiresApi
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.getViewRootImpl
import com.kieronquinn.app.taptap.utils.extensions.setBackgroundBlurRadius

@RequiresApi(Build.VERSION_CODES.R)
class BlurProvider30(resources: Resources): BlurProvider() {

    override val minBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.min_window_blur_radius).toFloat()
    }

    override val maxBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.max_window_blur_radius).toFloat()
    }

    private val blurDisabledSysProp by lazy {
        SystemProperties.getBoolean("persist.sys.sf.disable_blurs", false)
    }

    private val supportsBackgroundBlur by lazy {
        SystemProperties.getBoolean("ro.surface_flinger.supports_background_blur", false)
    }

    override fun applyDialogBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        applyBlurToWindow(dialogWindow, ratio)
    }

    override fun applyBlurToWindow(window: Window, ratio: Float) {
        if (!supportsBlursOnWindows()) {
            window.addDimming()
            return
        }
        window.clearDimming()
        val radius = blurRadiusOfRatio(ratio)
        val view = window.decorView
        runCatching {
            val viewRootImpl = view.getViewRootImpl() ?: return@runCatching
            val surfaceControl = viewRootImpl.surfaceControl ?: return@runCatching
            SurfaceControl.Transaction()
                .setBackgroundBlurRadius(surfaceControl, radius)
        }.onSuccess {
            return
        }
        //Re-add dimming due to failure
        window.addDimming()
    }

    private fun supportsBlursOnWindows(): Boolean {
        return supportsBackgroundBlur && !blurDisabledSysProp && ActivityManagerHidden.isHighEndGfx()
    }
}