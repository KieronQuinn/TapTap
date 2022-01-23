package com.kieronquinn.app.taptap.utils.extensions

import androidx.annotation.ColorInt
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath

fun LottieAnimationView.replaceColour(vararg keyPath: String, @ColorInt replaceWith: Int) {
    addValueCallback(KeyPath(*keyPath), LottieProperty.COLOR, {
        replaceWith
    })
}