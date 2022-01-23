package com.kieronquinn.app.taptap.utils.lazy

/**
 *  Wrapper to allow the same call as Dagger's .get() on a delayed function
 */
class LazyWrapper<T>(val getter: () -> T) {

    var isInitialized = false

    fun get(): T {
        isInitialized = true
        return getter()
    }

}