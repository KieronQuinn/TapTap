package com.kieronquinn.app.taptap.utils.extensions

import android.annotation.SuppressLint
import com.google.android.material.slider.Slider
import com.google.android.material.tooltip.TooltipDrawable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Slider.onChanged() = callbackFlow {
    val listener = Slider.OnChangeListener { _, value, fromUser ->
        if(fromUser){
            trySend(value)
        }
    }
    trySend(value)
    addOnChangeListener(listener)
    awaitClose {
        removeOnChangeListener(listener)
    }
}

@SuppressLint("RestrictedApi")
fun Slider.setTooltipColor(color: Int) {
    runCatching {
        val baseSlider = Slider::class.java.superclass ?: return
        val labels = baseSlider.getDeclaredField("labels").apply {
            isAccessible = true
        }
        (labels.get(this) as List<TooltipDrawable>).forEach {
            it.setTint(color)
        }
    }
}