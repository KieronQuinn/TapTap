package com.kieronquinn.app.taptap.ui.views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.px
import java.util.*

//Based on https://stackoverflow.com/a/52688461, modified to meet what was required
class RippleView : View, AnimatorUpdateListener {
    private inner class Ripple internal constructor(startRadiusFraction: Float, stopRadiusFraction: Float, startAlpha: Float, stopAlpha: Float, color: Int, delay: Long, duration: Long, strokeWidth: Float, updateListener: AnimatorUpdateListener?) {
        var mAnimatorSet: AnimatorSet
        var mRadiusAnimator: ValueAnimator
        var mAlphaAnimator: ValueAnimator
        var mPaint: Paint
        fun draw(canvas: Canvas, centerX: Int, centerY: Int, radiusMultiplicator: Float) {
            mPaint.alpha = (255 * mAlphaAnimator.animatedValue as Float).toInt()
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), mRadiusAnimator.animatedValue as Float * radiusMultiplicator, mPaint)
        }

        fun startAnimation() {
            mAnimatorSet.start()
        }

        fun stopAnimation() {
            mAnimatorSet.cancel()
        }

        init {
            mRadiusAnimator = ValueAnimator.ofFloat(startRadiusFraction, stopRadiusFraction)
            mRadiusAnimator.duration = duration
            mRadiusAnimator.addUpdateListener(updateListener)
            mRadiusAnimator.interpolator = DecelerateInterpolator()
            mAlphaAnimator = ValueAnimator.ofFloat(startAlpha, stopAlpha)
            mAlphaAnimator.duration = duration
            mAlphaAnimator.addUpdateListener(updateListener)
            mAlphaAnimator.interpolator = DecelerateInterpolator()
            mAnimatorSet = AnimatorSet()
            mAnimatorSet.playTogether(mRadiusAnimator, mAlphaAnimator)
            mAnimatorSet.startDelay = delay
            mPaint = Paint()
            mPaint.style = Paint.Style.STROKE
            mPaint.color = color
            mPaint.alpha = (255 * startAlpha).toInt()
            mPaint.isAntiAlias = true
            mPaint.strokeWidth = strokeWidth
        }
    }

    private var mRipples: MutableList<Ripple> = ArrayList()

    private val rippleColor by lazy {
        ContextCompat.getColor(context, R.color.ripple_circle)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private var videoRadius = 0f

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return

        post {
            val videoWidth = 95.px
            val maxRadius = measuredWidth / 2
            this.videoRadius = (videoWidth.toFloat() / maxRadius)
        }
    }

    fun startAnimation(numberRipples: Int) {
        visibility = VISIBLE
        mRipples = ArrayList()
        mRipples.add(Ripple(videoRadius, 1.25f, 1.0f, 0.0f, rippleColor, 0, 2000, 2.px.toFloat(), this))
        for(i in 1 until numberRipples){
            mRipples.add(Ripple(videoRadius, 1.25f, 1.0f, 0.0f, rippleColor, (i * 30).toLong(), 2000, 2.px.toFloat(), this))
        }
        mRipples.forEach {
            it.startAnimation()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2
        val radiusMultiplicator = width / 2
        for (ripple in mRipples) {
            ripple.draw(canvas, centerX, centerY, radiusMultiplicator.toFloat())
        }
    }

    companion object {
        const val TAG = "RippleView"
    }
}