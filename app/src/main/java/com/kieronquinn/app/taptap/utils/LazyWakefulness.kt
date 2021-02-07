package com.kieronquinn.app.taptap.utils

import com.android.systemui.keyguard.WakefulnessLifecycle
import dagger.Lazy

class LazyWakefulness(private val instance: WakefulnessLifecycle) : Lazy<WakefulnessLifecycle> {
    override fun get(): WakefulnessLifecycle {
        return instance
    }

}

val wakefulnessLifecycle by lazy {
    LazyWakefulness(WakefulnessLifecycle())
}