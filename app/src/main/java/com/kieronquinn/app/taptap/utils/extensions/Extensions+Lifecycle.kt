package com.kieronquinn.app.taptap.utils.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

fun Lifecycle.runOnDestroy(block: () -> Unit) {
    addObserver(object: LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            block()
        }
    })
}