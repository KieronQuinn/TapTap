package com.kieronquinn.app.taptap.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.monetcompat.core.MonetCompat
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KProperty1

/**
 *  Shows a expanding circular ripple when [addRipple] is called
 */
class RippleView: View {

    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int): super(context, attrs, defStyleRes)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null, 0)

    companion object {
        private const val RIPPLE_DURATION = 1000L
    }

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val singleTapColor by lazy {
        val fallbackBackground = if (context.isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        monet.getBackgroundColorSecondary(context) ?: fallbackBackground
    }

    private val doubleTapColor by lazy {
        monet.getPrimaryColor(context)
    }

    private val tripleTapColor by lazy {
        doubleTapColor
    }

    private val singleWidth by lazy {
        context.resources.getDimension(R.dimen.ripple_view_single_width)
    }

    private val doubleWidth by lazy {
        context.resources.getDimension(R.dimen.ripple_view_double_width)
    }

    private val tripleWidth by lazy {
        context.resources.getDimension(R.dimen.ripple_view_triple_width)
    }

    private val minRadius by lazy {
        (resources.getDimension(R.dimen.setup_gesture_lottie_size) / 2f)
    }

    private var maxRadius: Float = 0f
    private var cx = 0f
    private var cy = 0f

    private val ripples = ArrayList<Ripple>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxRadius = measuredWidth / 2f
        cx = measuredWidth / 2f
        cy = measuredHeight / 2f
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if(isVisible){
            ripples.forEach { it.resume() }
        }else{
            ripples.forEach { it.pause() }
        }
    }

    override fun onDetachedFromWindow() {
        ripples.toTypedArray().forEach {
            it.cancel()
        }
        super.onDetachedFromWindow()
    }

    fun addRipple(rippleType: RippleType) {
        ripples.add(Ripple(rippleType).apply {
            start()
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ripples.forEach {
            paint.strokeWidth = it.getWidth()
            paint.color = it.getColor()
            canvas.drawCircle(cx, cy, it.getRadius(), paint)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return ripples.toParcelableRipples(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? ParcelableRipples)?.let {
            ripples.clear()
            ripples.addAll(it.ripples.map { ripple -> ripple.toRipple().apply {
                start()
            }})
            super.onRestoreInstanceState(it.superState)
        } ?: super.onRestoreInstanceState(state)
    }

    enum class RippleType(val color: KProperty1<RippleView, Int>, val width: KProperty1<RippleView, Float>) {
        SINGLE_TAP(RippleView::singleTapColor, RippleView::singleWidth),
        DOUBLE_TAP(RippleView::doubleTapColor, RippleView::doubleWidth),
        TRIPLE_TAP(RippleView::tripleTapColor, RippleView::tripleWidth)
    }

    @Parcelize
    data class ParcelableRipple(val rippleType: String, val progress: Float): Parcelable

    @Parcelize
    data class ParcelableRipples(val ripples: List<ParcelableRipple>, val superState: Parcelable?): Parcelable

    private fun ParcelableRipple.toRipple(): Ripple {
        return Ripple(RippleType.valueOf(rippleType), progress)
    }

    private fun List<Ripple>.toParcelableRipples(superState: Parcelable?): ParcelableRipples {
        val ripples = map { it.toParcelableRipple() }
        return ParcelableRipples(ripples, superState)
    }

    inner class Ripple(private val rippleType: RippleType, startProgress: Float? = null) {

        private var progress = 0f

        private val rawColor by lazy {
            rippleType.color.get(this@RippleView)
        }

        private val rawWidth by lazy {
            rippleType.width.get(this@RippleView)
        }

        private val animator = ValueAnimator.ofFloat(startProgress ?: 0f, 1f).apply {
            val duration = startProgress?.let {
                (RIPPLE_DURATION - (startProgress * RIPPLE_DURATION)).roundToLong()
            } ?: RIPPLE_DURATION
            this.duration = duration
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            doOnEnd {
                ripples.remove(this@Ripple)
            }
            doOnCancel {
                ripples.remove(this@Ripple)
            }
        }

        fun start() {
            animator.start()
        }

        fun pause() {
            animator.pause()
        }

        fun resume() {
            animator.resume()
        }

        fun cancel() {
            animator.cancel()
        }

        fun getRadius(): Float {
            return (((maxRadius - minRadius) * progress) + minRadius)
        }

        fun getColor(): Int {
            val alpha = (255 * (1f - progress)).roundToInt()
            return Color.argb(alpha, rawColor.red, rawColor.green, rawColor.blue)
        }

        fun getWidth(): Float {
            return rawWidth
        }

        fun toParcelableRipple(): ParcelableRipple {
            return ParcelableRipple(rippleType.name, progress)
        }

    }

}