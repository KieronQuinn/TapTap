package com.kieronquinn.app.taptap.core

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.util.ArraySet
import android.util.Log
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.TfClassifier
import com.kieronquinn.app.taptap.core.columbus.actions.ActionBase
import com.kieronquinn.app.taptap.core.columbus.actions.DoNothingAction
import com.kieronquinn.app.taptap.core.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.core.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_FEEDBACK_WAKE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MAIN_SWITCH
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_RESTART_SERVICE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_SENSITIVITY
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH
import com.kieronquinn.app.taptap.core.services.TapForegroundService
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.core.smali.SmaliCalls
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.extensions.legacySharedPreferences
import com.kieronquinn.app.taptap.utils.extensions.sharedPreferences
import com.kieronquinn.app.taptap.core.workers.RestartWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class TapColumbusService(private val context: Context, private val tapFileRepository: TapFileRepository, private val tapSharedPreferences: TapSharedPreferences, gestureSensor: GestureSensor, powerManagerWrapper: PowerManagerWrapper):
    ColumbusService(emptyList(), emptySet(), emptySet(), gestureSensor, powerManagerWrapper, null), SharedPreferences.OnSharedPreferenceChangeListener, KoinComponent {

    private val tapServiceContainer by inject<TapServiceContainer>()
    private var isDemoMode: Boolean = false
    private val tapGestureSensor = gestureSensor as TapGestureSensorImpl

    companion object {
        const val TAG = "TapColumbusService"
    }

    private var lastActiveActionTriple: Action? = null
    var tripleTapActions: List<Action> = emptyList()

    init {
        Log.d(TAG, "init")
        this.gestureListener = TapGestureListener(this)
        gestureSensor.setGestureListener(gestureListener)
        context.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        context.legacySharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        refreshColumbusFeedback()
        if(tapSharedPreferences.isRestartEnabled){
            RestartWorker.queueRestartWorker(context)
        }
        with(tapFileRepository){
            scope.launch {
                doubleTapActions.collect {
                    withContext(Dispatchers.Main) {
                        setActions(it.getColumbusActions())
                        updateSensorListener()
                    }
                }
            }
            scope.launch {
                tripleTapActions.collect {
                    withContext(Dispatchers.Main) {
                        setTripleActions(it.getColumbusActions())
                        updateSensorListener()
                    }
                }
            }
            scope.launch {
                gates.collect {
                    if(isDemoMode) return@collect
                    withContext(Dispatchers.Main) {
                        val context = tapServiceContainer.accessibilityService ?: return@withContext
                        setGates(it.getGates(context))
                        updateSensorListener()
                    }
                }
            }
            getDoubleTapActions()
            getTripleTapActions()
            getGates()
        }
    }

    fun updateActiveActionTriple(): Action? {
        if(!isEnabled) return null
        val var1 = firstAvailableActionTriple()
        val var2 = lastActiveActionTriple
        if (var2 != null && var1 !== var2) {
            val var3 = java.lang.StringBuilder()
            var3.append("Switching action from ")
            var3.append(var2)
            var3.append(" to ")
            var3.append(var1)
            Log.i("Columbus/ColumbusService", var3.toString())
            var2.onProgress(0, null as GestureSensor.DetectionProperties?)
        }
        lastActiveActionTriple = var1
        return var1
    }

    private fun firstAvailableActionTriple(): Action? {
        return tripleTapActions.firstOrNull { it.isAvailable }
    }

    private fun setActions(actions: List<Action>){
        if(isDemoMode) return
        actions.forEach {
            it.listener = null
        }
        if(actions.isEmpty()){
            this.actions = listOf(DoNothingAction(context))
        }else{
            this.actions = actions.apply {
                forEach { it.listener = actionListener }
            }
        }
    }

    private fun setTripleActions(actions: List<Action>){
        if(isDemoMode) return
        actions.forEach {
            it.listener = null
        }
        if(actions.isEmpty()){
            this.tripleTapActions = listOf(DoNothingAction(context))
        }else{
            this.tripleTapActions = actions.apply {
                forEach { it.listener = actionListener }
            }
        }
    }

    fun ping(): Boolean {
        return true
    }

    private fun updateConfig(){
        val defaultModel = context.getDefaultTfModel()
        //Set model from prefs
        SmaliCalls.setTapRtModel(
            TfModel.valueOf(tapSharedPreferences.model).model
        )
        tapGestureSensor.customTap?.apply {
            val sensitivity = tapSharedPreferences.sensitivity
            positivePeakDetector.setMinNoiseTolerate(sensitivity)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when(key) {
            SHARED_PREFERENCES_KEY_MAIN_SWITCH -> {
                updateSensorListener()
            }
            SHARED_PREFERENCES_KEY_MODEL -> {
                val model = TfModel.valueOf(
                    sharedPreferences.getString(
                        SHARED_PREFERENCES_KEY_MODEL,
                        TfModel.PIXEL4.name
                    ) ?: TfModel.PIXEL4.name
                )
                tapGestureSensor.setTfClassifier(context.assets, model.model)
            }
            SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, SHARED_PREFERENCES_KEY_FEEDBACK_WAKE -> {
                //Refresh feedback options
                refreshColumbusFeedback()
            }
            SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH -> {
                //Set triple tap enabled
                tapGestureSensor.customTap?.isTripleTapEnabled = tapSharedPreferences.isTripleTapEnabled
            }
            SHARED_PREFERENCES_KEY_SENSITIVITY -> {
                //Reconfigure
                updateConfig()
            }
            SHARED_PREFERENCES_KEY_RESTART_SERVICE -> {
                //Enable/disable restart worker
                if(tapSharedPreferences.isRestartEnabled){
                    RestartWorker.queueRestartWorker(context)
                }else{
                    RestartWorker.clearRestartWorker(context)
                }
            }
        }
    }

    private fun Array<ActionInternal>.getColumbusActions(): List<Action> {
        return this.mapNotNull {
            ActionInternal.getActionForEnum(tapServiceContainer, it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }
    }

    private fun refreshColumbusFeedback() {
        val feedbackSet = getColumbusFeedback()
        effects = feedbackSet
    }

    private fun getColumbusFeedback(): Set<FeedbackEffect> {
        val sharedPreferences = context.legacySharedPreferences
        val isVibrateEnabled =
            sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, true) ?: true
        val isWakeEnabled = sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, true) ?: true
        val feedbackList = ArrayList<FeedbackEffect>()
        if (isVibrateEnabled) feedbackList.add(HapticClickCompat(context))
        if (isWakeEnabled) feedbackList.add(WakeDevice(context))
        return feedbackList.toSet()
    }

    private fun setGates(set: Set<Gate>){
        //Remove current gates' listeners
        gates.forEach {
            it.listener = null
            it.deactivate()
            it.listener = null
        }
        //Set new gates
        gates = set
        gates.forEach {
            it.listener = gateListener
        }
        updateSensorListener()
    }

    override fun isEnabled(): Boolean {
        if(isDemoMode) return true
        if(context == null) return false
        //(Re)start the service if required, this is called every time the action is run so is the best positioned to be run after it's been killed
        startServiceIfNeeded()
        return tapSharedPreferences.isMainEnabled
    }

    private fun startServiceIfNeeded(){
        if(!context.isServiceRunning(TapForegroundService::class.java)){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, TapForegroundService::class.java))
            }else{
                context.startService(Intent(context, TapForegroundService::class.java))
            }
        }
    }

    internal fun setDemoMode(enabled: Boolean){
        this.isDemoMode = enabled
        if(enabled){
            gates = emptySet()
            effects = setOf(HapticClickCompat(context, true))
            tapGestureSensor.customTap?.isTripleTapEnabled = true
        }else{
            with(tapFileRepository) {
                getDoubleTapActions()
                getTripleTapActions()
                getGates()
            }
            refreshColumbusFeedback()
            tapGestureSensor.customTap?.isTripleTapEnabled = tapSharedPreferences.isTripleTapEnabled
        }
        updateSensorListener()
    }

    override fun updateSensorListenerTriple() {
        //Update triple tap action
        updateActiveActionTriple()
    }

    private fun Array<GateInternal>.getGates(context: Context): Set<Gate> {
        val gates = ArraySet<Gate>()
        for(gate in this){
            if(!gate.isActivated) continue
            gates.add(getGate(context, gate.gate, gate.data) ?: continue)
        }
        return gates
    }

    private fun GestureSensorImpl.setTfClassifier(assetManager: AssetManager, tfModel: String){
        val tfClassifier = TfClassifier(assetManager, tfModel)
        tap._tflite = tfClassifier
    }

}