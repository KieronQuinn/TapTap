package com.google.android.columbus.sensors.configuration

import android.content.Context

abstract class Adjustment(private val context: Context) {

    private var callback: ((Adjustment) -> Unit)? = null

    abstract fun adjustSensitivity(sensitivity: Float): Float

    fun onSensitivityChanged() {
        callback?.invoke(this)
    }

    fun setCallback(callback: ((Adjustment) -> Unit)? = null){
        this.callback = callback
    }

}