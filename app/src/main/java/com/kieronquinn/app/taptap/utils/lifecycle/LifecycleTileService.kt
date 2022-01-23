package com.kieronquinn.app.taptap.utils.lifecycle

import android.content.Intent
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher

/**
 *  A [TileService] that is also a [LifecycleOwner].
 *
 *  Adapted from [LifecycleService](https://android.googlesource.com/platform/frameworks/support/+/a9ac247af2afd4115c3eb6d16c05bc92737d6305/lifecycle/service/src/main/java/androidx/lifecycle/LifecycleService.java)
 */
abstract class LifecycleTileService : TileService(), LifecycleOwner {

    private val mDispatcher by lazy {
        ServiceLifecycleDispatcher(this)
    }

    @CallSuper
    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onBind(intent: Intent?): IBinder? {
        mDispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @CallSuper
    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    // this method is added only to annotate it with @CallSuper.
    // In usual service super.onStartCommand is no-op, but in LifecycleService
    // it results in mDispatcher.onServicePreSuperOnStart() call, because
    // super.onStartCommand calls onStart().
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return mDispatcher.lifecycle
    }

}