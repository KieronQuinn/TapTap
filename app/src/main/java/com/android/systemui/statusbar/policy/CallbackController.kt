package com.android.systemui.statusbar.policy

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.DefaultLifecycleObserver

interface CallbackController : DefaultLifecycleObserver {
    fun addCallback(arg1: Any?)

    fun observe(arg2: Lifecycle, arg3: Any): Any? {
        arg2.addObserver(this)
        return arg3
    }

    fun observe(arg1: LifecycleOwner, arg2: Any): Any? {
        return this.observe(arg1.lifecycle, arg2)
    }

    fun removeCallback(arg1: Any?)

    override fun onResume(owner: LifecycleOwner) {
        addCallback(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        removeCallback(owner)
    }
}