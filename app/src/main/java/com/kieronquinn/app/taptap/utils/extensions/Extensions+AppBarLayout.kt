package com.kieronquinn.app.taptap.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

fun AppBarLayout.collapsedState() = callbackFlow {
    var cachedState = false
    val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        if(verticalOffset == 0){
            if(cachedState) {
                cachedState = false
                trySend(false)
            }
        }else if(abs(verticalOffset) >= totalScrollRange){
            if(!cachedState) {
                cachedState = true
                trySend(true)
            }
        }
    }
    addOnOffsetChangedListener(listener)
    awaitClose {
        removeOnOffsetChangedListener(listener)
    }
}

private const val FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED = "app_bar_collapsed"

fun Fragment.rememberAppBarCollapsed(collapsed: Boolean) {
    val arguments = this.arguments ?: Bundle()
    arguments.putBoolean(FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED, collapsed)
    this.arguments = arguments
}

fun Fragment.getRememberedAppBarCollapsed(): Boolean {
    return arguments?.getBoolean(FRAGMENT_ARGUMENTS_APP_BAR_COLLAPSED, false) ?: false
}