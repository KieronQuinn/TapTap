package com.kieronquinn.app.taptap.utils.foldable

import android.content.Context
import com.kieronquinn.app.taptap.utils.extensions.FoldingFeature_STATE_HALF_OPENED

class SidecarProvider(context: Context): FoldableProvider {

    private val sidecarImpl = Class.forName("androidx.window.sidecar.SidecarProvider").getMethod("getSidecarImpl", Context::class.java).invoke(null, context)

    private fun getDevicePosture(): Int {
        val sidecarDeviceState = sidecarImpl.javaClass.getMethod("getDeviceState").invoke(sidecarImpl)
        return sidecarDeviceState.javaClass.getField("posture").getInt(sidecarDeviceState)
    }

    override fun isClosed(): Boolean {
        return getDevicePosture() == FoldingFeature_STATE_HALF_OPENED
    }

    companion object {
        fun isDeviceFoldable(context: Context): Boolean {
            kotlin.runCatching {
                return SidecarProvider(context).getDevicePosture() != 0
            }
            return false
        }
    }

}