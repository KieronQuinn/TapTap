package com.kieronquinn.app.taptap.columbus.gates

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import com.google.android.systemui.columbus.gates.Gate

/*
    This one is slightly odd as a sensor listener doesn't stay running in the background to allow for asynchronous listening.
    We therefore use a little bit of a hacky way of detecting - attach a listener on a background thread and then immediately block the main thread waiting for the event (which is almost instantaneous)
 */

class PocketDetection(context: Context) : Gate(context) {

    private lateinit var handlerThread: HandlerThread
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val sensorListener = object: SensorEventCallback() {
        override fun onSensorChanged(event: SensorEvent?) {
            super.onSensorChanged(event)
            isSensorBlocking = event!!.values[0] == 0f
            sensorManager.unregisterListener(this)
        }
    }

    private var isSensorBlocking: Boolean? = null

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        val proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        this.handlerThread = HandlerThread(PocketDetection::class.java.simpleName)
        this.handlerThread.start()
        val handler = Handler(this.handlerThread.looper)
        sensorManager.registerListener(sensorListener, proximity, SensorManager.SENSOR_DELAY_NORMAL, handler)
        while(isSensorBlocking == null){}
        return isSensorBlocking!!
    }

}