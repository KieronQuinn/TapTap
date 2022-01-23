package com.google.android.columbus.sensors

interface Sensor {
    fun isListening(): Boolean
    fun startListening(heuristicMode: Boolean)
    fun stopListening()
}