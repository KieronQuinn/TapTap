package com.kieronquinn.app.taptap.utils

import android.content.Context

class SidecarProvider(context: Context) {

    private val sidecarImpl = Class.forName("androidx.window.sidecar.SidecarProvider").getMethod("getSidecarImpl", Context::class.java).invoke(null, context)

    fun getDevicePosture(): Int {
        val sidecarDeviceState = sidecarImpl.javaClass.getMethod("getDeviceState").invoke(sidecarImpl)
        return sidecarDeviceState.javaClass.getField("posture").getInt(sidecarDeviceState)
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