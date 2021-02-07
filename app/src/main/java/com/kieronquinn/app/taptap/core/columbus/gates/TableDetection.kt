package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import com.google.android.systemui.columbus.gates.Gate
import java.util.*
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt

//Loosely based on https://stackoverflow.com/questions/30948131/how-to-know-if-android-device-is-flat-on-table
class TableDetection(context: Context) : Gate(context), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isFlat = false
    private lateinit var handlerThread: HandlerThread

    override fun onActivate() {
        this.handlerThread = HandlerThread(UUID.randomUUID().toString())
        this.handlerThread.start()
        val handler = Handler(this.handlerThread.looper)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, handler)
    }

    override fun onDeactivate() {
        sensorManager.unregisterListener(this)
    }

    override fun isBlocked(): Boolean {
        return isFlat
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return

        // Movement
        var x = values[0]
        var y = values[1]
        var z = values[2]
        val norm = sqrt(x * x + y * y + (z * z).toDouble()).toFloat()

        // Normalize the accelerometer vector
        x /= norm
        y /= norm
        z /= norm
        val inclination = Math.toDegrees(acos(z.toDouble())).roundToInt()

        isFlat = inclination < 25 || inclination > 155
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}