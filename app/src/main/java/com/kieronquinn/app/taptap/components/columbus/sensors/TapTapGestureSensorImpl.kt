package com.kieronquinn.app.taptap.components.columbus.sensors

import android.content.Context
import android.hardware.SensorEvent
import android.os.Handler
import android.util.Log
import com.google.android.columbus.sensors.GestureSensorImpl
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import com.kieronquinn.app.taptap.utils.extensions.runOnClose
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

/**
 *  Extension of [GestureSensorImpl] which implements triple tap
 */
class TapTapGestureSensorImpl(
    context: Context,
    private val handler: Handler,
    isTripleTapEnabled: Boolean,
    settings: TapTapSettings,
    internal val scope: Scope,
    tapModel: TapModel,
    private val serviceEventEmitter: ServiceEventEmitter
) : GestureSensorImpl(
    context
) {

    companion object {
        /*
            SENSITIVITY
            These values get applied to the model's noise reduction. The higher the value, the more reduction of 'noise', and therefore the harder the gesture is to run.
            Anything from 0.0 to 0.1 should really work, but 0.75 is pretty hard to trigger so that's set to the maximum and values filled in from there
            For > 0.05f, the values were initially even spaced, but that put too much weight on the higher values which made the force difference between 0.05 (default) the next value too great
            Instead I made up some values that are semi-evenly spaced and seem to provide a decent weighting
            For < 0.05f, the values are evenly spaced down to 0 which is no noise removal at all and really easy to trigger.
         */
        val SENSITIVITY_VALUES =
            arrayOf(0.75f, 0.53f, 0.40f, 0.25f, 0.1f, 0.05f, 0.04f, 0.03f, 0.02f, 0.01f, 0.0f)
    }

    inner class GestureSensorEventListener : GestureSensorImpl.GestureSensorEventListener() {

        private var isKilled = false
        private var hasNotifiedStart = false

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || isKilled) return
            notifyStartIfNeeded()
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
            when (tap.checkDoubleTapTiming(event.timestamp)) {
                1 -> handler.post {
                    reportGestureDetected(2, DetectionProperties(true))
                }
                //Double tap
                2 -> handler.post {
                    Log.d("TTC", "reportGestureDetected from gesture sensor")
                    reportGestureDetected(1, DetectionProperties(false))
                }
                //Triple tap
                3 -> handler.post {
                    reportGestureDetected(3, DetectionProperties(false))
                }
            }
        }

        @Synchronized
        private fun notifyStartIfNeeded() {
            if(hasNotifiedStart) return
            hasNotifiedStart = true
            GlobalScope.launch {
                serviceEventEmitter.postServiceEvent(ServiceEventEmitter.ServiceEvent.Started)
            }
        }

        init {
            scope.runOnClose {
                //Stop sensor listener on close
                setListening(false, 0)
                isKilled = true
            }
        }
    }

    private fun getSensitivityValueForLevel(sensitivityLevel: Int): Float {
        return SENSITIVITY_VALUES.getOrNull(sensitivityLevel)
            ?: SENSITIVITY_VALUES[TapTapSettingsImpl.DEFAULT_COLUMBUS_SENSITIVITY_LEVEL]
    }

    override val sensorEventListener = GestureSensorEventListener()
    override val tap by lazy {
        TapTapTapRT(
            context,
            160000000L,
            context.assets,
            isTripleTapEnabled,
            getSensitivityValueForLevel(settings.columbusSensitivityLevel.getSync()),
            tapModel,
            scope,
            settings
        )
    }

}