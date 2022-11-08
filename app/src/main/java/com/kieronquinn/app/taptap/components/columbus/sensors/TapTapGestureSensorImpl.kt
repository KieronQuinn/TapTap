package com.kieronquinn.app.taptap.components.columbus.sensors

import android.content.Context
import android.content.res.AssetManager
import android.hardware.SensorEvent
import android.os.Handler
import com.google.android.columbus.sensors.GestureSensorImpl
import com.samsung.android.backtap.SamsungBackTapDetectionService
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl.Companion.DEFAULT_COLUMBUS_SENSITIVITY_LEVEL
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
    private val tapModel: TapModel,
    private val serviceEventEmitter: ServiceEventEmitter
) : GestureSensorImpl(
    context
) {

    companion object {
        /*
            COLUMBUS SENSITIVITY
            These values get applied to the model's noise reduction. The higher the value, the more reduction of 'noise', and therefore the harder the gesture is to run.
            Anything from 0.0 to 0.1 should really work, but 0.75 is pretty hard to trigger so that's set to the maximum and values filled in from there
            For > 0.05f, the values were initially even spaced, but that put too much weight on the higher values which made the force difference between 0.05 (default) the next value too great
            Instead I made up some values that are semi-evenly spaced and seem to provide a decent weighting
            For < 0.05f, the values are evenly spaced down to 0 which is no noise removal at all and really easy to trigger.
         */
        val COLUMBUS_SENSITIVITY_VALUES =
            arrayOf(0.75f, 0.53f, 0.40f, 0.25f, 0.1f, 0.05f, 0.04f, 0.03f, 0.02f, 0.01f, 0.0f)

        /*
            SAMSUNG SENSITIVITY
            Unlike Columbus, these values are applied as a multiplier during peak detection.
         */
        val SAMSUNG_SENSITIVITY_VALUES =
            arrayOf(0.25f, 0.4f, 0.55f, 0.7f, 0.85f, 1f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f)
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

    override fun startListening(heuristicMode: Boolean) {
        if(tapModel.modelType != TapModel.ModelType.OEM) {
            super.startListening(heuristicMode)
        }else{
            sensorEventListener.setListening(true, 0)
        }
    }

    private fun getSensitivityValueForLevel(sensitivityLevel: Int): Float {
        return when(tapModel){
            TapModel.SAMSUNG -> {
                SAMSUNG_SENSITIVITY_VALUES.getOrNull(sensitivityLevel)
                    ?: SAMSUNG_SENSITIVITY_VALUES[DEFAULT_COLUMBUS_SENSITIVITY_LEVEL]
            }
            else -> {
                COLUMBUS_SENSITIVITY_VALUES.getOrNull(sensitivityLevel)
                    ?: COLUMBUS_SENSITIVITY_VALUES[DEFAULT_COLUMBUS_SENSITIVITY_LEVEL]
            }
        }
    }

    override val sensorEventListener = GestureSensorEventListener()
    override val tap by lazy {
        val classifier = createClassifier(context.assets, settings)
        val customSensitivity = settings.columbusCustomSensitivity.getSync()
        val sensitivity = if(customSensitivity == -1f){
            getSensitivityValueForLevel(settings.columbusSensitivityLevel.getSync())
        }else customSensitivity
        if(tapModel.modelType == TapModel.ModelType.OEM){
            when(tapModel){
                TapModel.SAMSUNG -> {
                    SamsungBackTapDetectionService(isTripleTapEnabled, sensitivity, classifier)
                }
                else -> throw RuntimeException("Invalid model")
            }
        }else{
            TapTapTapRT(160000000L, isTripleTapEnabled, sensitivity, classifier)
        }
    }

    private fun createClassifier(
        assetManager: AssetManager,
        settings: TapTapSettings
    ): TapTapTfClassifier {
        return TapTapTfClassifier(assetManager, tapModel, scope, settings)
    }

}