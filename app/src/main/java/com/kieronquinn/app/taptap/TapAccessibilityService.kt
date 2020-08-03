package com.kieronquinn.app.taptap

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
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
import com.kieronquinn.app.taptap.impl.KeyguardStateControllerImpl
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.store.ActionListFile
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*

class TapAccessibilityService : AccessibilityService(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TAS"
    }

    private var columbusService: ColumbusService? = null
    private var gestureSensorImpl: GestureSensorImpl? = null

    private var currentPackageName: String = "android"

    private val keyguardStateController by lazy {
        KeyguardStateControllerImpl()
    }

    private var wakefulnessLifecycle: WakefulnessLifecycle? = null

    private val sharedPreferences by lazy {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val context = this
        val activityManagerService = try{
            ActivityManager::class.java.getMethod("getService").invoke(null)
        }catch (e: NoSuchMethodException){
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration)
        val powerManagerWrapper = PowerManagerWrapper(context)
        val metricsLogger = MetricsLogger()
        val wakefulnessLifecycle = WakefulnessLifecycle()
        this.wakefulnessLifecycle = wakefulnessLifecycle

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        //Set model from prefs
        SmaliCalls.setTapRtModel(TfModel.valueOf(sharedPreferences.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name) ?: TfModel.PIXEL4.name).model)

        //Create the service
        this.columbusService = ColumbusService::class.java.constructors.first().newInstance(getColumbusActions(), getColumbusFeedback(), getGates(context), gestureSensorImpl, powerManagerWrapper, metricsLogger) as ColumbusService
    }

    private fun getColumbusActions() : List<Action> {
        return ActionListFile.loadFromFile(this).toList().mapNotNull { getActionForEnum(it) }
    }

    private fun getActionForEnum(action: ActionInternal) : Action? {
        return try {
            when (action.action) {
                TapAction.LAUNCH_CAMERA -> LaunchCameraLocal(
                    this
                )
                TapAction.BACK -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_BACK
                )
                TapAction.HOME -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_HOME
                )
                TapAction.LOCK_SCREEN -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_LOCK_SCREEN
                )
                TapAction.RECENTS -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_RECENTS
                )
                TapAction.SCREENSHOT -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_TAKE_SCREENSHOT
                )
                TapAction.QUICK_SETTINGS -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_QUICK_SETTINGS
                )
                TapAction.NOTIFICATIONS -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_NOTIFICATIONS
                )
                TapAction.SPLIT_SCREEN -> AccessibilityServiceGlobalAction(
                    this,
                    GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
                )
                TapAction.FLASHLIGHT -> Flashlight(this)
                TapAction.LAUNCH_APP -> LaunchApp(this, action.data ?: "")
                TapAction.LAUNCH_SHORTCUT -> LaunchShortcut(this, action.data ?: "")
                TapAction.LAUNCH_ASSISTANT -> LaunchAssistant(this)
                TapAction.TASKER_EVENT -> TaskerEvent(this)
                TapAction.TASKER_TASK -> TaskerTask(this, action.data ?: "")
                TapAction.TOGGLE_PAUSE -> MusicAction(this, MusicAction.Command.TOGGLE_PAUSE)
                TapAction.PREVIOUS -> MusicAction(this, MusicAction.Command.PREVIOUS)
                TapAction.NEXT -> MusicAction(this, MusicAction.Command.NEXT)
                TapAction.VOLUME_PANEL -> VolumeAction(this, AudioManager.ADJUST_SAME)
                TapAction.VOLUME_UP -> VolumeAction(this, AudioManager.ADJUST_RAISE)
                TapAction.VOLUME_DOWN -> VolumeAction(this, AudioManager.ADJUST_LOWER)
                TapAction.VOLUME_TOGGLE_MUTE -> VolumeAction(this, AudioManager.ADJUST_TOGGLE_MUTE)
                TapAction.WAKE_DEVICE -> WakeDeviceAction(this)
            }
        }catch (e: RuntimeException){
            //Enum not found, probably a downgrade issue
            null
        }
    }

    private fun refreshColumbusActions(){
        columbusService?.setActions(getColumbusActions())
    }

    private fun createGestureConfiguration(context: Context, activityManager: Any): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first().newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet(), factory)
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        //Stop the service to prevent listeners still being attached
        columbusService?.stop()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName?.toString() != currentPackageName) {
            if(event.packageName?.toString() == "android") return
            currentPackageName = event.packageName?.toString() ?: "android"
            Log.d(TAG, "package $currentPackageName isCamera ${isPackageCamera(currentPackageName)}")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        refreshColumbusActions()
        if(key == SHARED_PREFERENCES_KEY_MODEL){
            val model = TfModel.valueOf(sharedPreferences.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name) ?: TfModel.PIXEL4.name)
            gestureSensorImpl?.setTfClassifier(assets, model.model)
        }
        if(SHARED_PREFERENCES_FEEDBACK_KEYS.contains(key)){
            //Refresh feedback options
            refreshColumbusFeedback()
        }
        if(key == SHARED_PREFERENCES_KEY_GATES){
            //Refresh gates
            refreshColumbusGates(this)
        }
        if(key == SHARED_PREFERENCES_KEY_ACTIONS_TIME){
            //Refresh actions
            refreshColumbusActions()
        }
    }

    private fun refreshColumbusFeedback(){
        val feedbackSet = getColumbusFeedback()
        Log.d(TAG, "Setting feedback to ${feedbackSet.joinToString(", ")}")
        columbusService?.setFeedback(feedbackSet)
    }

    private fun getColumbusFeedback(): Set<FeedbackEffect> {
        val isVibrateEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, true)
        val isWakeEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, true)
        val feedbackList = ArrayList<FeedbackEffect>()
        if(isVibrateEnabled) feedbackList.add(HapticClickCompat(this))
        if(isWakeEnabled) feedbackList.add(WakeDevice(this))
        return feedbackList.toSet()
    }

    private fun refreshColumbusGates(context: Context){
        val gatesSet = getGates(context)
        Log.d(TAG, "setting gates to ${gatesSet.joinToString(", ")}")
        columbusService?.setGates(gatesSet)
    }

    fun getCurrentPackageName(): String {
        return currentPackageName
    }
}