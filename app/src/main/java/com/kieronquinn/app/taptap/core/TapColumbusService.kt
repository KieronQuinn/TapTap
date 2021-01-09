package com.kieronquinn.app.taptap.core

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.columbus.actions.ActionBase
import com.kieronquinn.app.taptap.columbus.actions.DoNothingAction
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_ACTIONS_TIME
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_FEEDBACK_WAKE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_GATES
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MAIN_SWITCH
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_RESTART_SERVICE
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_SENSITIVITY
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH
import com.kieronquinn.app.taptap.core.services.TapForegroundService
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.models.store.DoubleTapActionListFile
import com.kieronquinn.app.taptap.models.store.TripleTapActionListFile
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.workers.RestartWorker
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class TapColumbusService(private val context: Context, private val tapSharedPreferences: TapSharedPreferences, gestureSensor: GestureSensor, powerManagerWrapper: PowerManagerWrapper):
    ColumbusService(emptyList(), emptySet(), emptySet(), gestureSensor, powerManagerWrapper, null), SharedPreferences.OnSharedPreferenceChangeListener, KoinComponent {

    private val tapServiceContainer by inject<TapServiceContainer>()
    private var isDemoMode: Boolean = false
    private val tapGestureSensor = gestureSensor as TapGestureSensorImpl

    companion object {
        const val TAG = "TapColumbusService"
    }

    private var lastActiveActionTriple: Action? = null
    var tripleTapActions: MutableList<Action> = emptyList<Action>().toMutableList()

    init {
        Log.d(TAG, "init")
        this.gestureListener = TapGestureListener(this)
        gestureSensor.setGestureListener(gestureListener)
        context.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        refreshColumbusActions()
        refreshColumbusActionsTriple()
        refreshColumbusFeedback()
        refreshColumbusGates(context)
        if(tapSharedPreferences.isRestartEnabled){
            RestartWorker.queueRestartWorker(context)
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
        Log.d(TAG, "firstAvailableActionTriple actions size ${tripleTapActions.size}")
        tripleTapActions.forEach {
            Log.d(TAG, "firstAvailableActionTriple ${it.javaClass.simpleName} isAvailable ${it.isAvailable}")
        }
        return tripleTapActions.firstOrNull { it.isAvailable }
    }

    fun setActions(actions: List<Action>){
        if(actions.isEmpty()){
            this.actions = listOf(DoNothingAction(context))
        }else{
            this.actions = actions
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
            SHARED_PREFERENCES_KEY_GATES -> {
                //Refresh gates
                refreshColumbusGates(tapServiceContainer.accessibilityService!!)
            }
            SHARED_PREFERENCES_KEY_ACTIONS_TIME -> {
                //Refresh actions
                refreshColumbusActions()
            }
            SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME -> {
                //Refresh triple tap actions
                refreshColumbusActionsTriple()
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

    private fun refreshColumbusActions() {
        setActions(getColumbusActions())
    }

    private fun refreshColumbusActionsTriple() {
        tripleTapActions = getColumbusActionsTriple()
    }

    private fun getColumbusActions(): List<Action> {
        return DoubleTapActionListFile.loadFromFile(context).toList().mapNotNull {
            ActionInternal.getActionForEnum(tapServiceContainer, it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }
    }

    private fun getColumbusActionsTriple(): MutableList<Action> {
        return TripleTapActionListFile.loadFromFile(context).toList().mapNotNull {
            ActionInternal.getActionForEnum(tapServiceContainer, it).apply {
                (this as? ActionBase)?.triggerListener = {
                    Log.d(TAG, "action trigger from type ${context.javaClass.simpleName}")
                }
            }
        }.toMutableList()
    }

    private fun refreshColumbusFeedback() {
        val feedbackSet = getColumbusFeedback()
        effects = feedbackSet
    }

    private fun getColumbusFeedback(): Set<FeedbackEffect> {
        val sharedPreferences = context.sharedPreferences
        val isVibrateEnabled =
            sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, true) ?: true
        val isWakeEnabled = sharedPreferences?.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, true) ?: true
        val feedbackList = ArrayList<FeedbackEffect>()
        if (isVibrateEnabled) feedbackList.add(HapticClickCompat(context))
        if (isWakeEnabled) feedbackList.add(WakeDevice(context))
        return feedbackList.toSet()
    }

    private fun refreshColumbusGates(context: Context) {
        val gatesSet = getGates(context)
        Log.d(TAG, "setting gates to ${gatesSet.joinToString(", ")}")
        setGates(gatesSet)
    }

    private fun setGates(set: Set<Gate>){
        //Remove current gates' listeners
        gates.forEach {
            it.listener = null
            it.deactivate()
        }
        //Set new gates
        gates = set
        val gateListener = ColumbusService::class.java.getDeclaredField("gateListener").setAccessibleR(true).get(this) as Gate.Listener
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
            refreshColumbusActions()
            refreshColumbusActionsTriple()
            refreshColumbusFeedback()
            refreshColumbusGates(context)
            tapGestureSensor.customTap?.isTripleTapEnabled = tapSharedPreferences.isTripleTapEnabled
        }
    }

}