package com.kieronquinn.app.taptap

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.internal.logging.MetricsLogger
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.android.systemui.statusbar.policy.KeyguardStateController
import com.google.android.systemui.columbus.*
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.*
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.columbus.actions.Flashlight
import com.kieronquinn.app.taptap.columbus.actions.LaunchApp
import com.kieronquinn.app.taptap.columbus.actions.LaunchAssistant
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.columbus.gates.CameraVisibility
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
        val contentResolverWrapper = ContentResolverWrapper(context)
        val activityManagerService = ActivityManager::class.java.getMethod("getService").invoke(null)
        //val columbusContentObserver = ColumbusContentObserverCompat.Factory(contentResolverWrapper, activityManagerService)
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration)
        val powerManagerWrapper = PowerManagerWrapper(context)
        val metricsLogger = MetricsLogger()
        val wakefulnessLifecycle = WakefulnessLifecycle()
        this.wakefulnessLifecycle = wakefulnessLifecycle
        val mainHandler = Handler()

        //Feedback
        val hapticClick =
            HapticClickCompat(
                context
            )

        //Gates
        val powerState = PowerState(context,
            LazyWakefulness(wakefulnessLifecycle)
        )

        //We can't create this properly as it's missing some classes, so we'll hack it with reflection
        val keyguardVisibility = KeyguardVisibility::class.java.getConstructor(Context::class.java, KeyguardStateController::class.java).newInstance(context, keyguardStateController)
        //val cameraVisibility = CameraVisibility(context, emptyList(), keyguardVisibility, powerState, activityManagerService, mainHandler)

        val chargingState = ChargingState(context, mainHandler, ColumbusModule.provideTransientGateDuration())
        val telephonyActivity = TelephonyActivity(context)
        val usbState = UsbState(context, mainHandler, ColumbusModule.provideTransientGateDuration())
        //val vrMode = VrMode(context)

        /*val navigationBarControllerClass = XposedHelpers.findClass("com.android.systemui.statusbar.NavigationBarController", classLoader)
        val actualNavigationBarController = classLoader.getDependency(navigationBarControllerClass)
        val navigationBarController = NavigationBarController(actualNavigationBarController, navigationBarControllerClass)

        val navUndimEffect = NavUndimEffect::class.java.getConstructor(NavigationBarController::class.java).newInstance(navigationBarController)
        val assistManagerClass = XposedHelpers.findClass("com.android.systemui.assist.AssistManager", classLoader)
        val assistManager = classLoader.getDependency(assistManagerClass)
        val assistInvocationEffect = AssistInvocationEffectCompat(assistManager, assistManagerClass)*/

        val cameraVisibility = CameraVisibility(this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        //Set model from prefs
        SmaliCalls.setTapRtModel(TfModel.valueOf(sharedPreferences.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name) ?: TfModel.PIXEL4.name).model)

        //Create the service
        //this.columbusService = ColumbusService(getColumbusActions(), getColumbusFeedback(), getGates(context), gestureSensorImpl, powerManagerWrapper, metricsLogger)
        this.columbusService = ColumbusService::class.java.constructors.first().newInstance(getColumbusActions(), getColumbusFeedback(), getGates(context), gestureSensorImpl, powerManagerWrapper, metricsLogger) as ColumbusService
    }

    private fun getColumbusActions() : List<Action> {
        return ActionListFile.loadFromFile(this).toList().map { getActionForEnum(it) }
    }

    private fun getActionForEnum(action: ActionInternal) : Action {
        return when(action.action){
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
            TapAction.FLASHLIGHT -> Flashlight(this)
            TapAction.LAUNCH_APP -> LaunchApp(this, action.data ?: "")
            TapAction.LAUNCH_ASSISTANT -> LaunchAssistant(this)
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