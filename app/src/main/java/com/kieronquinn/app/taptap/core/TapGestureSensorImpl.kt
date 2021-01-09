package com.kieronquinn.app.taptap.core

import android.content.Context
import android.hardware.SensorEvent
import com.google.android.systemui.columbus.sensors.CustomTapRT
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.utils.setAccessibleR

class TapGestureSensorImpl(context: Context, tapSharedPreferences: TapSharedPreferences, gestureConfiguration: GestureConfiguration): GestureSensorImpl(context, gestureConfiguration) {

    val customTap
        get() = tap as? CustomTapRT

    init {
        customTap?.isTripleTapEnabled = tapSharedPreferences.isTripleTapEnabled
        sensorEventListener = object : GestureSensorEventListener() {

            init {
                GestureSensorEventListener::class.java.getDeclaredField("this\$0")
                    .setAccessibleR(true).set(this, this@TapGestureSensorImpl)
            }

            override fun onSensorChanged(arg14: SensorEvent) {
                val sensor = arg14.sensor
                val v14 = customTap?.run {
                    updateData(
                        sensor.type,
                        arg14.values[0],
                        arg14.values[1],
                        arg14.values[2],
                        arg14.timestamp,
                        samplingIntervalNs,
                        isRunningInLowSamplingRate
                    )
                    checkDoubleTapTiming(arg14.timestamp)
                }
                if (v14 == 1) {
                    val timeout = this.onTimeout
                    handler.removeCallbacks(timeout)
                    handler.post {
                        if (listener != null) {
                            val detectionProperties =
                                GestureSensor.DetectionProperties(false, true, 1)
                            listener.onGestureProgress(this@TapGestureSensorImpl, 1, detectionProperties)
                        }
                        handler.postDelayed(timeout, TIMEOUT_MS)
                    }
                } else if (v14 == 2) {
                    val timeout = this.onTimeout
                    handler.removeCallbacks(timeout)
                    handler.post {
                        if (listener != null) {
                            val detectionProperties =
                                GestureSensor.DetectionProperties(false, false, 2)
                            listener.onGestureProgress(this@TapGestureSensorImpl, 3, detectionProperties)
                        }
                        `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                    }
                } else if (v14 == 3) {
                    val timeout = this.onTimeout
                    handler.removeCallbacks(timeout)
                    handler.post {
                        if (listener != null) {
                            val detectionProperties =
                                GestureSensor.DetectionProperties(false, false, 3)
                            listener.onGestureProgress(this@TapGestureSensorImpl, 3, detectionProperties)
                        }
                        `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                    }
                }
            }

        }
    }
}