package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.media.AudioManager
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.isAppLaunchable

class VolumeAction(context: Context, private val action: Int, whenGates: List<WhenGateInternal>) : ActionBase(context, whenGates) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onTrigger() {
        super.onTrigger()
        audioManager.adjustVolume(action, AudioManager.FLAG_SHOW_UI)
    }


}