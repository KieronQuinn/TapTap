package com.kieronquinn.app.taptap.services

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.store.ActionListFile
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*

class TapSharedComponent(private val context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TapService"
        private const val MESSAGE_START = 1001
    }

    private var columbusService: ColumbusService? = null
    private var gestureSensorImpl: GestureSensorImpl? = null

    private var wakefulnessLifecycle: WakefulnessLifecycle? = null

    lateinit var accessibilityService: TapAccessibilityService

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun getColumbusActions(): List<Action> {
        return ActionListFile.loadFromFile(accessibilityService).toList().mapNotNull {
            getActionForEnum(it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }
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
                TapAction.SOUND_PROFILER -> SoundProfileAction(context, action.whenList)
                TapAction.WAKE_DEVICE -> WakeDeviceAction(context, action.whenList)
                TapAction.GOOGLE_VOICE_ACCESS -> GoogleVoiceAccessAction(context, action.whenList)
                TapAction.LAUNCH_SEARCH -> LaunchSearch(context, action.whenList)
            }
        } catch (e: RuntimeException) {
            //Enum not found, probably a downgrade issue
            null
        }
    }

    private fun refreshColumbusActions() {
        columbusService?.setActions(getColumbusActions())
    }

    private fun createGestureConfiguration(
        context: Context,
        activityManager: Any
    ): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first()
            .newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet(), factory)
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
        }
    }

    fun startTap() {
        val activityManagerService = try {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        } catch (e: NoSuchMethodException) {
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration)
        val powerManagerWrapper = PowerManagerWrapper(context)
        val metricsLogger = MetricsLogger()
        val wakefulnessLifecycle = WakefulnessLifecycle()
        this.wakefulnessLifecycle = wakefulnessLifecycle

        //Set model from prefs
        SmaliCalls.setTapRtModel(
            TfModel.valueOf(
                sharedPreferences.getString(
                    SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name
                ) ?: TfModel.PIXEL4.name
            ).model
        )

        //Create the service
        this.columbusService = ColumbusService::class.java.constructors.first().newInstance(
            getColumbusActions(),
            getColumbusFeedback(),
            getGates(accessibilityService),
            gestureSensorImpl,
            powerManagerWrapper,
            metricsLogger
        ) as ColumbusService
        configureTap()

        context.sendBroadcast(
            Intent(TapAccessibilityService.KEY_ACCESSIBILITY_START).setPackage(
                context.packageName
            )
        )
    }

    fun stopTap() {
        columbusService?.stop()
    }

}