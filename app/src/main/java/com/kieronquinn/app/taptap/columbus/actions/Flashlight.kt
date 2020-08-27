package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.models.WhenGateInternal

class Flashlight(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    private var isFlashlightOn = false

    private val torchCallback = object: CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            isFlashlightOn = enabled
        }
    }

    override fun setListener(p0: Listener?) {
        super.setListener(p0)
        if(listener != null) {
            cameraManager.registerTorchCallback(torchCallback, Handler())
        }else{
            cameraManager.unregisterTorchCallback(torchCallback)
        }
    }

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun isAvailable(): Boolean {
        if(!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return false
        return super.isAvailable()
    }

    override fun onTrigger() {
        super.onTrigger()
        try {
            isFlashlightOn = !isFlashlightOn
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
        }catch (e: CameraAccessException){
            //Camera in use
        }
    }


}