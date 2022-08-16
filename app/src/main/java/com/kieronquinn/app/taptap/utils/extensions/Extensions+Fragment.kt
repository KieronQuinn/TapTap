package com.kieronquinn.app.taptap.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Fragment.childBackStackTopFragment() = callbackFlow {
    val listener = FragmentManager.OnBackStackChangedListener {
        trySend(getTopFragment())
    }
    childFragmentManager.addOnBackStackChangedListener(listener)
    trySend(getTopFragment())
    awaitClose {
        childFragmentManager.removeOnBackStackChangedListener(listener)
    }
}

fun Fragment.getTopFragment(): Fragment? {
    if(!isAdded) return null
    return childFragmentManager.fragments.firstOrNull()
}