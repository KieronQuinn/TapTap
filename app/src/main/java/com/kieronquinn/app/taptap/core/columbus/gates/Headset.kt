package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.google.android.systemui.columbus.gates.Gate

open class Headset(context: Context) : Gate(context) {

    private val headsetTypes = arrayOf(
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_USB_HEADSET
    )

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val devices: Array<AudioDeviceInfo> = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (headsetTypes.contains(device.type)) {
                return true
            }
        }
        return false
    }

}