package com.kieronquinn.app.taptap.components.blur

import android.app.ActivityManager
import android.content.res.Resources
import android.os.Build
import android.view.SurfaceControl
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.SystemProperties_getBoolean

@RequiresApi(Build.VERSION_CODES.R)
class BlurProvider30(resources: Resources): BlurProvider() {

    override val minBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.min_window_blur_radius).toFloat()
    }

    override val maxBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.max_window_blur_radius).toFloat()
    }

    private val getViewRootImpl by lazy {
        try {
            View::class.java.getMethod("getViewRootImpl")
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    private val viewRootImpl by lazy {
        try {
            Class.forName("android.view.ViewRootImpl")
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    private val getSurfaceControl by lazy {
        try {
            viewRootImpl?.getMethod("getSurfaceControl")
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    private val setBackgroundBlurRadius by lazy {
        try {
            SurfaceControl.Transaction::class.java.getMethod(
                "setBackgroundBlurRadius",
                SurfaceControl::class.java,
                Integer.TYPE
            )
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    private val blurDisabledSysProp by lazy {
        SystemProperties_getBoolean("persist.sys.sf.disable_blurs", false)
    }

    private val supportsBackgroundBlur by lazy {
        SystemProperties_getBoolean("ro.surface_flinger.supports_background_blur", false)
    }

    private val isHighEndGfx by lazy {
        try {
            ActivityManager::class.java.getMethod("isHighEndGfx").invoke(null) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
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
            val viewRootImpl = getViewRootImpl?.invoke(view) ?: return
            val surfaceControl =
                getSurfaceControl?.invoke(viewRootImpl) as? SurfaceControl ?: return
            val transaction = SurfaceControl.Transaction()
            setBackgroundBlurRadius?.invoke(transaction, surfaceControl, radius)
            transaction.apply()
        }.onFailure {
            //Re-add dimming due to failure
            window.addDimming()
        }
    }

    private fun supportsBlursOnWindows(): Boolean {
        return supportsBackgroundBlur && !blurDisabledSysProp && isHighEndGfx
    }
}