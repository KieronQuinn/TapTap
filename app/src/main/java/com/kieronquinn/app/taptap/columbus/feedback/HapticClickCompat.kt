package com.kieronquinn.app.taptap.columbus.feedback

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_NAME
import com.kieronquinn.app.taptap.utils.settingsGlobalGetIntOrNull

class HapticClickCompat(private val context: Context) : FeedbackEffect {

    private val progressVibrationEffect: VibrationEffect? = getVibrationEffect(0)
    private val contentResolver: ContentResolver = context.contentResolver
    private val resolveVibrationEffect: VibrationEffect? = getVibrationEffect(5)
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val dndMode
        get() = settingsGlobalGetIntOrNull(contentResolver, "zen_mode")
    private val overrideDnd
        get() = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND, false)

    companion object {
        @SuppressLint("WrongConstant")
        private val SONIFICATION_AUDIO_ATTRIBUTES = AudioAttributes.Builder().setContentType(4).setUsage(13).build()
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