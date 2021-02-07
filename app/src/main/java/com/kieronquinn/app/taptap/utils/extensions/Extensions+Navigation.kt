package com.kieronquinn.app.taptap.utils.extensions

import android.content.Context
import android.content.Intent
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.kieronquinn.app.taptap.R

fun Intent.toActivityDestination(context: Context): ActivityNavigator.Destination {
    return ActivityNavigator(context).createDestination().apply {
        intent = this@toActivityDestination
    }
}

fun NavOptions.Builder.withStandardAnimations(): NavOptions.Builder {
    setEnterAnim(R.anim.slide_in_right)
    setExitAnim(R.anim.slide_out_left)
    setPopEnterAnim(R.anim.slide_in_left)
    setPopExitAnim(R.anim.slide_out_right)
    return this
}