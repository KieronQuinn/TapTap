package com.kieronquinn.app.taptap.utils.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.ArrayRes
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import kotlin.math.ceil
import kotlin.math.sqrt

fun Resources.getResourceIdArray(@ArrayRes resourceId: Int): Array<Int> {
    val array = obtainTypedArray(resourceId)
    val items = mutableListOf<Int>()
    for(i in 0 until array.length()){
        items.add(array.getResourceId(i, 0))
    }
    array.recycle()
    return items.toTypedArray()
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

//This is for the normal size ONLY and should NEVER be used when windowInsets are available
fun getStaticStatusBarHeight(context: Context): Int {
    val resources: Resources = context.resources
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else ceil(
        ((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 24 else 25) * resources.displayMetrics.density).toDouble()
    ).toInt()
}

fun getSpannedText(text: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(text)
    }
}

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun Context.getToolbarHeight(): Int {
    val tv = TypedValue()
    if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    }
    return 0
}

@ColorRes
fun Context.resolveColorAttribute(@AttrRes res: Int): Int {
    val tv = TypedValue()
    if (theme.resolveAttribute(res, tv, true)) {
        return tv.resourceId
    }
    return 0
}

fun View.animateBackgroundStateChange(@ColorInt beforeColor: Int? = null, @ColorInt afterColor: Int): ValueAnimator {
    val before = beforeColor ?: (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    return ValueAnimator.ofObject(ArgbEvaluator(), before, afterColor).apply {
        duration = 250
        addUpdateListener {
            this@animateBackgroundStateChange.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        start()
    }
}

fun View.animateElevationChange(afterElevation: Float): ValueAnimator {
    return ValueAnimator.ofFloat(elevation, afterElevation).apply {
        duration = 250
        addUpdateListener {
            this@animateElevationChange.elevation = it.animatedValue as Float
        }
        start()
    }
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

fun Context.getPhysicalScreenSize(): Double {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    manager.defaultDisplay.getMetrics(displayMetrics)
    val widthInInches : Float = displayMetrics.widthPixels / displayMetrics.xdpi
    val heightInInches : Float = displayMetrics.heightPixels / displayMetrics.ydpi
    val ab = (widthInInches * widthInInches) + (heightInInches * heightInInches).toDouble()
    return sqrt(ab)
}