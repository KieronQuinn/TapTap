package com.kieronquinn.app.taptap.utils.extensions

import android.annotation.SuppressLint
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun NavController.onDestinationChanged() = callbackFlow {
    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        trySend(destination)
    }
    addOnDestinationChangedListener(listener)
    currentDestination?.let {
        trySend(it)
    }
    awaitClose {
        removeOnDestinationChangedListener(listener)
    }
}

@SuppressLint("RestrictedApi")
fun NavController.hasBackAvailable(): Boolean {
    //Seems to include the root
    return currentBackStack.value.size > 2
}

fun NavController.setOnBackPressedCallback(callback: OnBackPressedCallback) {
    NavController::class.java.getDeclaredField("onBackPressedCallback").apply {
        isAccessible = true
    }.set(this, callback)
}