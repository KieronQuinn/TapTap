package com.kieronquinn.app.taptap.utils.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.ColorInt

fun View.fadeIn(callback: (() -> Unit)? = null) {

    //Don't run animation if the view is already visible
    if (visibility == View.VISIBLE && alpha == 1f)
        return

    val fadeInAnimation = AlphaAnimation(0f, 1f)
    fadeInAnimation.duration = 500
    fadeInAnimation.fillAfter = true
    fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            callback?.invoke()
        }
    })
    visibility = View.VISIBLE
    startAnimation(fadeInAnimation)
}

fun View.fadeOut(endVisibility: Int = View.INVISIBLE, callback: (() -> Unit)? = null) {

    //Don't run animation if the view is already invisible
    if (visibility != View.VISIBLE)
        return

    val fadeOutAnimation = AlphaAnimation(1f, 0f)
    fadeOutAnimation.duration = 500
    fadeOutAnimation.fillAfter = true
    fadeOutAnimation.fillBefore = false
    fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            callback?.invoke()
        }

        override fun onAnimationStart(animation: Animation?) {
            visibility = endVisibility
        }

    })
    startAnimation(fadeOutAnimation)
}

fun View.animateBackgroundTint(@ColorInt toColor: Int){
    val fromColor = backgroundTintList?.defaultColor ?: Color.TRANSPARENT
    ValueAnimator().apply {
        setIntValues(fromColor, toColor)
        setEvaluator(ArgbEvaluator())
        addUpdateListener {
            this@animateBackgroundTint.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        duration = 1000L
    }.start()
}

fun View.animateColorChange(@ColorInt beforeColor: Int? = null, @ColorInt afterColor: Int): ValueAnimator {
    val before = beforeColor ?: (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    return ValueAnimator.ofObject(ArgbEvaluator(), before, afterColor).apply {
        duration = 250
        addUpdateListener {
            this@animateColorChange.setBackgroundColor(it.animatedValue as Int)
        }
        start()
    }
}