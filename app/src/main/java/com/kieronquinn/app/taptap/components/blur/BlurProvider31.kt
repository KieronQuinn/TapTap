package com.kieronquinn.app.taptap.components.blur

import android.content.res.Resources
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import com.kieronquinn.app.taptap.R

@RequiresApi(Build.VERSION_CODES.S)
class BlurProvider31(resources: Resources): BlurProvider() {

    override val minBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.min_window_blur_radius).toFloat()
    }

    override val maxBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.max_window_blur_radius).toFloat()
    }

    override fun applyDialogBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        val radius = blurRadiusOfRatio(ratio)
        if(radius == 0){
            appWindow.decorView.setRenderEffect(null)
        }else {
            val renderEffect = RenderEffect.createBlurEffect(radius.toFloat(), radius.toFloat(), Shader.TileMode.MIRROR)
            appWindow.decorView.setRenderEffect(renderEffect)
        }
    }

    override fun applyBlurToWindow(window: Window, ratio: Float) {
        val radius = blurRadiusOfRatio(ratio)
        window.setBackgroundBlurRadius(radius)
    }

}