package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import androidx.window.FoldingFeature
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.utils.SidecarProvider

class FoldableClosed(context: Context): Gate(context) {

    private val sidecarProvider by lazy {
        try{
            SidecarProvider(context)
        }catch (e: Exception){
            //Sidecar is not supported
            null
        }
    }

    override fun isBlocked(): Boolean {
        return sidecarProvider?.getDevicePosture() == FoldingFeature.STATE_FLAT
    }

    override fun onActivate() {}

    override fun onDeactivate() {}

}