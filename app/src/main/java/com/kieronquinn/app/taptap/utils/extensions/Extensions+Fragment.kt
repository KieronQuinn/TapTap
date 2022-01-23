package com.kieronquinn.app.taptap.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
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

fun Fragment.awaitResultAsFlow(requestKey: String, onReady: suspend () -> Unit) = callbackFlow {
    val resultListener = { returnedRequestKey: String, result: Bundle ->
        if(returnedRequestKey == requestKey){
            trySend(result)
        }
    }
    setFragmentResultListener(requestKey, resultListener)
    onReady()
    awaitClose {
        setFragmentResultListener(requestKey) { _, _ ->
            //Do nothing
        }
    }
}