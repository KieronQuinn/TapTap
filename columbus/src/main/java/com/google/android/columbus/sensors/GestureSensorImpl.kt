package com.google.android.columbus.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import com.kieronquinn.app.shared.taprt.BaseTapRT

open class GestureSensorImpl(
    context: Context
): GestureSensor() {

    open inner class GestureSensorEventListener: SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //no-op
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if(event == null) return
            val sensorType = event.sensor.type
            tap.updateData(
                sensorType,
                event.values[0],
                event.values[1],
                event.values[2],
                event.timestamp,
                samplingIntervalNs,
                isRunningInLowSamplingRate
            )
            val timing = tap.checkDoubleTapTiming(event.timestamp)
            when(timing){
                1 -> handler.post {
                    reportGestureDetected(2, DetectionProperties(true))
                }
                2 -> handler.post {
                    reportGestureDetected(1, DetectionProperties(false))
                }
            }
        }

        fun setListening(listening: Boolean, samplingPeriod: Int) {
            if(listening && accelerometer != null && gyroscope != null) {
                sensorManager.registerListener(this, accelerometer, samplingPeriod, handler)
                sensorManager.registerListener(this, gyroscope, samplingPeriod, handler)
                setListening(true)
            }else{
                sensorManager.unregisterListener(this)
                setListening(false)
            }
        }

    }

    private val handler = Handler(Looper.getMainLooper())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(1)
    private val gyroscope = sensorManager.getDefaultSensor(4)
    open val sensorEventListener = GestureSensorEventListener()
    protected val samplingIntervalNs = 2500000L
    protected val isRunningInLowSamplingRate = false
    private var isListening = false
    open val tap: BaseTapRT = TapRT(160000000L)

    override fun isListening(): Boolean {
        return isListening
    }

    fun setListening(listening: Boolean) {
        this.isListening = listening
    }

    override fun startListening(heuristicMode: Boolean) {
        if(heuristicMode) {
            sensorEventListener.setListening(true, 0)
            (tap as? TapRT)?.run {
                getLowpassKey().setPara(0.2f)
                getHighpassKey().setPara(0.2f)
                getPositivePeakDetector().setMinNoiseTolerate(0.05f)
                getPositivePeakDetector().setWindowSize(0x40)
                reset(false)
            }
        }else{
            sensorEventListener.setListening(true, 21000)
            (tap as? TapRT)?.run {
                getLowpassKey().setPara(1f)
                getHighpassKey().setPara(0.3f)
                getPositivePeakDetector().setMinNoiseTolerate(0.02f)
                getPositivePeakDetector().setWindowSize(8)
                getNegativePeakDetection().setMinNoiseTolerate(0.02f)
                getNegativePeakDetection().setWindowSize(8)
                reset(true)
            }
        }
    }

    override fun stopListening() {
        sensorEventListener.setListening(false, 0)
    }

}