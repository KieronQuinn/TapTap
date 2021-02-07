package com.kieronquinn.app.taptap.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.toActivityDestination
import com.kieronquinn.app.taptap.utils.extensions.withStandardAnimations

private fun Context.createCustomTabsIntent(url: String): CustomTabsIntent {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder().apply {
            setToolbarColor(ContextCompat.getColor(this@createCustomTabsIntent, R.color.windowBackground))
        }.build())
        .build()
    customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    customTabsIntent.intent.data = Uri.parse(url)
    return customTabsIntent
}


fun Context.launchCCT(url: String){
    createCustomTabsIntent(url).run {
        ActivityNavigator(this@launchCCT).navigate(intent.toActivityDestination(this@launchCCT), startAnimationBundle, NavOptions.Builder().withStandardAnimations().build(), null)
    }
}