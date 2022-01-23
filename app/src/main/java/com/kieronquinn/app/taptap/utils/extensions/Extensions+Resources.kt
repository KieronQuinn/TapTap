package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.annotation.ArrayRes
import kotlin.math.ceil

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

fun Resources.getResourceIdArray(@ArrayRes resourceId: Int): Array<Int> {
    val array = obtainTypedArray(resourceId)
    val items = mutableListOf<Int>()
    for(i in 0 until array.length()){
        items.add(array.getResourceId(i, 0))
    }
    array.recycle()
    return items.toTypedArray()
}
