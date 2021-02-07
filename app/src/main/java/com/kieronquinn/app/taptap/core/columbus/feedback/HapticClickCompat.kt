package com.kieronquinn.app.taptap.core.columbus.feedback

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.settingsGlobalGetIntOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HapticClickCompat(private val context: Context, private val forceOverrideDnd: Boolean = false) : FeedbackEffect, KoinComponent {

    private val tapSharedPreferences by inject<TapSharedPreferences>()

    private val progressVibrationEffect: VibrationEffect? = getVibrationEffect(0)
    private val contentResolver: ContentResolver = context.contentResolver
    private val resolveVibrationEffect: VibrationEffect? = getVibrationEffect(5)
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val dndMode
        get() = settingsGlobalGetIntOrNull(
            contentResolver,
            "zen_mode"
        )
    private val overrideDnd
        get() = tapSharedPreferences.overrideDnd || forceOverrideDnd

    companion object {
        @SuppressLint("WrongConstant")
        val SONIFICATION_AUDIO_ATTRIBUTES = AudioAttributes.Builder().setContentType(4).setUsage(13).build()
    }

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var2?.isHapticConsumed == false && (dndMode == 0 || overrideDnd)) {
            if (var1 == 3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && resolveVibrationEffect != null) {
                    vibrator.vibrate(resolveVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES)
                } else {
                    vibrator.vibrate(300)
                }
            } else if (var1 == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && progressVibrationEffect != null) {
                    vibrator.vibrate(progressVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES)
                } else {
                    vibrator.vibrate(200)
                }
            }
        }
    }

    private fun getVibrationEffect(effectId: Int): VibrationEffect? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(effectId)
        } else null
    }
}