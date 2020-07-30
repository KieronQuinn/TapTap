package com.android.systemui.columbus.feedback

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.jvm.internal.Intrinsics

class AssistInvocationEffectCompat(arg2: Any?, private val clazz: Class<*>) : FeedbackEffect {

    private var animation: AnimatorSet
    private var animatorUpdateListener = AnimatorUpdateListener { animation ->
        if (animation != null) {
            val v0 =
                this@AssistInvocationEffectCompat
            val v2 = animation.animatedValue
            if (v2 != null) {
                v0.progress = v2 as Float
                updateAssistManager()
                return@AnimatorUpdateListener
            }
            throw TypeCastException("null cannot be cast to non-null type kotlin.Float")
        }
    }
    private val assistManager: Any?
    private var progress = 0f

    companion object {
        private var DECAY_DURATION: Long = 0

        init {
            DECAY_DURATION =
                TimeUnit.SECONDS.toMillis(5L)
        }
    }

    private fun assistManagerShouldUseHomeButtonAnimations(): Boolean {
        return try {
            clazz.getMethod("shouldUseHomeButtonAnimations").invoke(assistManager) as Boolean
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    private fun assistManagerOnInvocationProgress(int: Int, float: Float): Boolean {
        return try {
            clazz.getMethod("onInvocationProgress", Integer.TYPE, Float::class.java).invoke(assistManager, int, float) as Boolean
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    // com.google.android.systemui.columbus.feedback.FeedbackEffect
    override fun onProgress(arg2: Int, arg3: DetectionProperties?) {
        if (assistManager != null && assistManagerShouldUseHomeButtonAnimations()) {
            return
        }
        if (arg2 != 1) {
            if (arg2 != 3) {
                setProgress(0.0f, false)
                return
            }
            setProgress(1.0f, false)
            return
        }
        setProgress(0.99f, true)
    }

    private fun setProgress(arg6: Float, arg7: Boolean) {
        if (animation.isRunning) {
            animation.cancel()
        }
        if (arg7) {
            animation = AnimatorSet()
            val v0 = ValueAnimator.ofFloat(*floatArrayOf(progress, arg6))
            Intrinsics.checkExpressionValueIsNotNull(v0, "toValueAnimator")
            v0.duration = 200L
            v0.interpolator = DecelerateInterpolator()
            v0.addUpdateListener(animatorUpdateListener)
            animation.play(v0)
            if (arg6 > 0.0f) {
                val v7 = ValueAnimator.ofFloat(*floatArrayOf(arg6, 0.0f))
                Intrinsics.checkExpressionValueIsNotNull(v7, "decayAnimator")
                v7.duration = (DECAY_DURATION.toFloat() * arg6).toLong()
                v7.interpolator = LinearInterpolator()
                v7.addUpdateListener(animatorUpdateListener)
                animation.play(v7).after(v0)
            }
            animation.start()
            return
        }
        progress = arg6
        updateAssistManager()
    }

    private fun updateAssistManager() {
        if (assistManager != null) {
            assistManagerOnInvocationProgress(0, progress)
        }
    }

    init {
        assistManager = arg2
        animation = AnimatorSet()
    }
}