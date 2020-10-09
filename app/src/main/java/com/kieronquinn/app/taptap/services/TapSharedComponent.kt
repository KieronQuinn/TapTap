package com.kieronquinn.app.taptap.services

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorEvent
import android.media.AudioManager
import android.util.Log
import com.android.internal.logging.MetricsLogger
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.google.android.systemui.columbus.ColumbusContentObserver
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.ContentResolverWrapper
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.CustomTapRT
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.Adjustment
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.models.store.DoubleTapActionListFile
import com.kieronquinn.app.taptap.models.store.TripleTapActionListFile
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*

class TapSharedComponent(private val context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TapService"
        private var INSTANCE: TapSharedComponent? = null

        fun getInstance(context: Context): TapSharedComponent {
            if(INSTANCE == null) INSTANCE = TapSharedComponent(context)
            return INSTANCE!!
        }
    }

    private var columbusService: ColumbusService? = null
    private var gestureSensorImpl: GestureSensorImpl? = null

    private var wakefulnessLifecycle: WakefulnessLifecycle? = null

    lateinit var accessibilityService: TapAccessibilityService

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun getColumbusActions(): List<Action> {
        return DoubleTapActionListFile.loadFromFile(accessibilityService).toList().mapNotNull {
            getActionForEnum(it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }
    }

    private fun getColumbusActionsTriple(): MutableList<Action> {
        return TripleTapActionListFile.loadFromFile(accessibilityService).toList().mapNotNull {
            getActionForEnum(it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }.toMutableList()
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun getActionForEnum(action: ActionInternal): Action? {
        val context = accessibilityService
        return try {
            when (action.action) {
                TapAction.LAUNCH_CAMERA -> LaunchCamera(context, action.whenList)
                TapAction.BACK -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_BACK,
                    action.whenList
                )
                TapAction.HOME -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_HOME,
                    action.whenList
                )
                TapAction.LOCK_SCREEN -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN,
                    action.whenList
                )
                TapAction.RECENTS -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_RECENTS,
                    action.whenList
                )
                TapAction.SPLIT_SCREEN -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN,
                    action.whenList
                )
                TapAction.REACHABILITY -> LaunchReachability(context, action.whenList)
                TapAction.SCREENSHOT -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT,
                    action.whenList
                )
                TapAction.QUICK_SETTINGS -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS,
                    action.whenList
                )
                TapAction.NOTIFICATIONS -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS,
                    action.whenList
                )
                TapAction.POWER_DIALOG -> AccessibilityServiceGlobalAction(
                    context,
                    AccessibilityService.GLOBAL_ACTION_POWER_DIALOG,
                    action.whenList
                )
                TapAction.FLASHLIGHT -> Flashlight(context, action.whenList)
                TapAction.LAUNCH_APP -> LaunchApp(context, action.data ?: "", action.whenList)
                TapAction.LAUNCH_SHORTCUT -> LaunchShortcut(
                    context,
                    action.data ?: "",
                    action.whenList
                )
                TapAction.LAUNCH_ASSISTANT -> LaunchAssistant(context, action.whenList)
                TapAction.TASKER_EVENT -> TaskerEvent(context, action.whenList)
                TapAction.TASKER_TASK -> TaskerTask(context, action.data ?: "", action.whenList)
                TapAction.TOGGLE_PAUSE -> MusicAction(
                    context,
                    MusicAction.Command.TOGGLE_PAUSE,
                    action.whenList
                )
                TapAction.PREVIOUS -> MusicAction(
                    context,
                    MusicAction.Command.PREVIOUS,
                    action.whenList
                )
                TapAction.NEXT -> MusicAction(context, MusicAction.Command.NEXT, action.whenList)
                TapAction.VOLUME_PANEL -> VolumeAction(
                    context,
                    AudioManager.ADJUST_SAME,
                    action.whenList
                )
                TapAction.VOLUME_UP -> VolumeAction(
                    context,
                    AudioManager.ADJUST_RAISE,
                    action.whenList
                )
                TapAction.VOLUME_DOWN -> VolumeAction(
                    context,
                    AudioManager.ADJUST_LOWER,
                    action.whenList
                )
                TapAction.VOLUME_TOGGLE_MUTE -> VolumeAction(
                    context,
                    AudioManager.ADJUST_TOGGLE_MUTE,
                    action.whenList
                )
                TapAction.ALARM_TIMER -> AlarmTimerAction(context, action.whenList)
                TapAction.ALARM_SNOOZE -> AlarmSnoozeAction(context, action.whenList)
                TapAction.SOUND_PROFILER -> SoundProfileAction(context, action.whenList)
                TapAction.WAKE_DEVICE -> WakeDeviceAction(context, action.whenList)
                TapAction.GOOGLE_VOICE_ACCESS -> GoogleVoiceAccessAction(context, action.whenList)
                TapAction.LAUNCH_SEARCH -> LaunchSearch(context, action.whenList)
                TapAction.HAMBURGER -> HamburgerAction(accessibilityService, action.whenList)
                TapAction.APP_DRAWER -> AccessibilityServiceGlobalAction(context, 14, action.whenList)
                TapAction.ALT_TAB -> AltTabAction(context, action.whenList)
                TapAction.ACCESSIBILITY_BUTTON_CHOOSER -> AccessibilityServiceGlobalAction(context, 12, action.whenList)
                TapAction.ACCESSIBILITY_SHORTCUT -> AccessibilityServiceGlobalAction(context, 13, action.whenList)
                TapAction.ACCESSIBILITY_BUTTON -> AccessibilityServiceGlobalAction(context, 11, action.whenList)
            }
        } catch (e: RuntimeException) {
            //Enum not found, probably a downgrade issue
            null
        }
    }

    private fun refreshColumbusActions() {
        columbusService?.setActions(getColumbusActions())
    }

    private fun refreshColumbusActionsTriple() {
        columbusService?.setActionsTriple(getColumbusActionsTriple())
    }

    private fun createGestureConfiguration(
        context: Context,
        activityManager: Any
    ): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first()
            .newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet<Adjustment>(), factory)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        refreshColumbusActions()
        if (key == SHARED_PREFERENCES_KEY_MODEL) {
            val model = TfModel.valueOf(
                sharedPreferences.getString(
                    SHARED_PREFERENCES_KEY_MODEL,
                    TfModel.PIXEL4.name
                ) ?: TfModel.PIXEL4.name
            )
            gestureSensorImpl?.setTfClassifier(context.assets, model.model)
        }
        if (SHARED_PREFERENCES_FEEDBACK_KEYS.contains(key)) {
            //Refresh feedback options
            refreshColumbusFeedback()
        }
        if (key == SHARED_PREFERENCES_KEY_GATES) {
            //Refresh gates
            refreshColumbusGates(accessibilityService)
        }
        if (key == SHARED_PREFERENCES_KEY_ACTIONS_TIME) {
            //Refresh actions
            refreshColumbusActions()
        }
        if (key == SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME) {
            //Refresh triple tap actions
            refreshColumbusActionsTriple()
        }
        if(key == SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH){
            //Set triple tap enabled
            (gestureSensorImpl?.getTapRT() as? CustomTapRT)?.isTripleTapEnabled = context.isTripleTapEnabled
        }
        if (key == SHARED_PREFERENCES_KEY_SENSITIVITY) {
            //Reconfigure
            configureTap()
        }

    }

    private fun refreshColumbusFeedback() {
        val feedbackSet = getColumbusFeedback()
        Log.d(TAG, "Setting feedback to ${feedbackSet.joinToString(", ")}")
        columbusService?.setFeedback(feedbackSet)
    }

    private fun getColumbusFeedback(): Set<FeedbackEffect> {
        val isVibrateEnabled =
            sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, true)
        val isWakeEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, true)
        val feedbackList = ArrayList<FeedbackEffect>()
        if (isVibrateEnabled) feedbackList.add(HapticClickCompat(context))
        if (isWakeEnabled) feedbackList.add(WakeDevice(context))
        return feedbackList.toSet()
    }

    private fun refreshColumbusGates(context: Context) {
        val gatesSet = getGates(context)
        Log.d(TAG, "setting gates to ${gatesSet.joinToString(", ")}")
        columbusService?.setGates(gatesSet)
    }

    fun getCurrentPackageName(): String {
        return accessibilityService.getCurrentPackageName()
    }

    private fun configureTap() {
        gestureSensorImpl?.getTapRT()?.run {
            val sensitivity =
                sharedPreferences.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")
                    ?.toFloatOrNull() ?: 0.05f
            Log.d(
                "TapRT",
                "getMinNoiseToTolerate ${positivePeakDetector.getMinNoiseToTolerate()} sensitivity $sensitivity"
            )
            positivePeakDetector.setMinNoiseTolerate(sensitivity)
            //TAP_TIMEOUT = 500000000L
        }
    }

    fun startTap() {
        //Stop previous one if valid
        gestureSensorImpl?.run {
            stopTap()
            stopListening()
        }
        val activityManagerService = try {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        } catch (e: NoSuchMethodException) {
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration).apply {
            (getTapRT() as? CustomTapRT)?.isTripleTapEnabled = context.isTripleTapEnabled
            sensorEventListener = object: GestureSensorImpl.GestureSensorEventListener(){

                init {
                    GestureSensorImpl.GestureSensorEventListener::class.java.getDeclaredField("this\$0").setAccessibleR(true).set(this, this@apply)
                }

                override fun onSensorChanged(arg14: SensorEvent) {
                    val sensor = arg14.sensor
                    val v14 = this@apply.tap.run {
                        updateData(sensor.type, arg14.values[0], arg14.values[1], arg14.values[2], arg14.timestamp, samplingIntervalNs, isRunningInLowSamplingRate)
                        checkDoubleTapTiming(arg14.timestamp)
                    }
                    if(v14 == 1){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                val detectionProperties = GestureSensor.DetectionProperties(false, true, 1)
                                listener.onGestureProgress(this@apply, 1, detectionProperties)
                            }
                            handler.postDelayed(timeout, GestureSensorImpl.TIMEOUT_MS)
                        }
                    }else if(v14 == 2){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                val detectionProperties = GestureSensor.DetectionProperties(false, false, 2)
                                listener.onGestureProgress(this@apply, 3, detectionProperties)
                            }
                            `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                        }
                    }else if(v14 == 3){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                val detectionProperties = GestureSensor.DetectionProperties(false, false, 3)
                                listener.onGestureProgress(this@apply, 3, detectionProperties)
                            }
                            `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                        }
                    }
                }

            }
        }
        val powerManagerWrapper = PowerManagerWrapper(context)
        val metricsLogger = MetricsLogger()
        val wakefulnessLifecycle = WakefulnessLifecycle()
        this.wakefulnessLifecycle = wakefulnessLifecycle

        val defaultModel = context.getDefaultTfModel()
        //Set model from prefs
        SmaliCalls.setTapRtModel(
            TfModel.valueOf(
                sharedPreferences.getString(
                    SHARED_PREFERENCES_KEY_MODEL, defaultModel.name
                ) ?: defaultModel.name
            ).model
        )

        //Create the service
        this.columbusService = TapColumbusService(
            context,
            getColumbusActions(),
            getColumbusActionsTriple(),
            getColumbusFeedback(),
            getGates(accessibilityService),
            gestureSensorImpl!!,
            powerManagerWrapper
        )

        configureTap()
    }

    fun stopTap() {
        gestureSensorImpl?.stopListening()
        columbusService?.stop()
    }

}