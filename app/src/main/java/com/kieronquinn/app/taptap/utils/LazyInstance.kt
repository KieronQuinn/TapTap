package com.kieronquinn.app.taptap.utils

import dagger.Lazy

class LazyInstance<T>(private val obj: T) : Lazy<T> {
    override fun get(): T {
        return obj
    }
}