package com.google.android.columbus.gates

import android.content.Context
import android.os.Handler
import android.os.Looper

abstract class Gate(
    val context: Context,
    private val notifyHandler: Handler = Handler(Looper.getMainLooper())
) {

    interface Listener {
        fun onGateChanged(gate: Gate)
    }

    var active: Boolean = false
    private var isBlocked = false
    protected val listeners = LinkedHashSet<Listener>()

    open val isBlocking
        get() = active && isBlocked

    fun maybeActivate(){
        if(!active && listeners.isNotEmpty()){
            active = true
            onActivate()
        }
    }

    fun maybeDeactivate(){
        if(active && listeners.isEmpty()){
            active = false
            onDeactivate()
        }
    }

    fun notifyListeners(){
        if(active){
            listeners.forEach {
                notifyHandler.post {
                    it.onGateChanged(this)
                }
            }
        }
    }

    abstract fun onActivate()
    abstract fun onDeactivate()

    fun registerListener(listener: Listener){
        listeners.add(listener)
        maybeActivate()
    }

    fun setBlocking(blocking: Boolean){
        if(isBlocked != blocking){
            isBlocked = blocking
            notifyListeners()
        }
    }

    override fun toString(): String {
        return this::class.java.simpleName
    }

    fun unregisterListener(listener: Listener){
        listeners.remove(listener)
        maybeDeactivate()
    }

}