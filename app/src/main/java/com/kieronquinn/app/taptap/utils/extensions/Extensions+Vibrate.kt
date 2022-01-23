package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

fun Context.vibrate(vibrationEffectId: Int, fallback: Long) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> vibrateS(vibrationEffectId)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> vibrateQ(vibrationEffectId)
        else -> vibrateFallback(fallback)
    }
}

private fun Context.vibrateFallback(fallback: Long){
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val attributes = AudioAttributes.Builder().apply {
        setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
    }.build()
    vibrator.vibrate(fallback, attributes)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.vibrateQ(vibrationEffectId: Int) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(vibrationEffectId)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun Context.vibrateS(vibrationEffectId: Int) {
    val vibrationService = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibrationService.defaultVibrator.vibrate(vibrationEffectId)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Vibrator.vibrate(vibrationEffectId: Int) {
    val attributes = AudioAttributes.Builder().apply {
        setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
    }.build()
    vibrate(VibrationEffect.createPredefined(vibrationEffectId), attributes)
}
