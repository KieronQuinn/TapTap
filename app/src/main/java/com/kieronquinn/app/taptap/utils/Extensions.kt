package com.kieronquinn.app.taptap.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.ArraySet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.google.android.systemui.columbus.ColumbusModule
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.*
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.TapRT
import com.google.android.systemui.columbus.sensors.TfClassifier
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.columbus.gates.AppVisibility
import com.kieronquinn.app.taptap.columbus.gates.CameraVisibility
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.store.GateListFile
import com.kieronquinn.app.taptap.providers.SharedPrefsProvider
import de.robv.android.xposed.XposedHelpers
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Field
import java.lang.reflect.Method

const val SHARED_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}_prefs"
const val SHARED_PREFERENCES_KEY_ACTION = "action"
const val SHARED_PREFERENCES_KEY_ACTIONS_TIME = "actions_time"
const val SHARED_PREFERENCES_KEY_GATES_TIME = "gates_time"
const val SHARED_PREFERENCES_KEY_GATES = "gates"
const val SHARED_PREFERENCES_KEY_MODEL = "model"
const val SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE = "feedback_vibrate"
const val SHARED_PREFERENCES_KEY_FEEDBACK_WAKE = "feedback_wake"

//Not currently implemented in Columbus (TODO figure out how the ML settings work and provide sensitivity options)
const val SHARED_PREFERENCES_KEY_SENSITIVITY = "sensitivity"

val SHARED_PREFERENCES_FEEDBACK_KEYS = arrayOf(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE)

val DEFAULT_GATES = arrayOf(TapGate.POWER_STATE, TapGate.TELEPHONY_ACTIVITY)
val ALL_NON_CONFIG_GATES = arrayOf(TapGate.POWER_STATE, TapGate.USB_STATE, TapGate.TELEPHONY_ACTIVITY, TapGate.CHARGING_STATE)

val DEFAULT_ACTIONS = arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.SCREENSHOT)

fun InputStream.copyFile(out: OutputStream) {
    val buffer = ByteArray(1024)
    var read: Int
    while (this.read(buffer).also { read = it } != -1) {
        out.write(buffer, 0, read)
    }
}

//Replaces hidden API
val ApplicationInfo.isSystemApp: Boolean
    get() {
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (flags and mask) == 0;
    }

fun Context.isAppLaunchable(packageName: String): Boolean {
    return try{
        packageManager.getLaunchIntentForPackage(packageName) != null
    }catch (e: Exception){
        false
    }
}

fun ColumbusService.setActions(list: List<Action>){
    ColumbusService::class.java.getDeclaredField("actions").setAccessibleR(true).set(this, list)
}

fun ColumbusService.setFeedback(set: Set<FeedbackEffect>){
    ColumbusService::class.java.getDeclaredField("effects").setAccessibleR(true).set(this, set)
}

fun ColumbusService.setGates(set: Set<Gate>){
    //Remove current gates' listeners
    gates.forEach {
        it.listener = null
        it.deactivate()
    }
    //Set new gates
    gates.clear()
    gates.addAll(set)
    val gateListener = ColumbusService::class.java.getDeclaredField("gateListener").setAccessibleR(true).get(this) as Gate.Listener
    gates.forEach {
        it.listener = gateListener
    }
    run<Unit>("updateSensorListener")
}

fun minApi(api: Int): Boolean {
    return Build.VERSION.SDK_INT >= api
}

private fun <T> ColumbusService.run(methodName: String, vararg params: Any?): T {
    return ColumbusService::class.java.getDeclaredMethod(methodName).setAccessibleR(true).invoke(this, params) as T
}

private fun <T> ColumbusService.run(methodName: String): T {
    return ColumbusService::class.java.getDeclaredMethod(methodName).setAccessibleR(true).invoke(this) as T
}

fun ColumbusService.stop(){
    run<Unit>("stopListening")
}

fun getGatesInternal(context: Context): List<GateInternal> {
    return GateListFile.loadFromFile(context).toList()
}

fun getGates(context: Context): Set<Gate> {
    val gates = ArraySet<Gate>()
    val gatesInternal = getGatesInternal(context)
    for(gate in gatesInternal){
        if(!gate.isActivated) continue
        gates.add(when (gate.gate) {
            TapGate.POWER_STATE -> PowerState(context, getWakefulnessLifecycle())
            TapGate.CHARGING_STATE -> ChargingState(context, Handler(), ColumbusModule.provideTransientGateDuration())
            TapGate.TELEPHONY_ACTIVITY -> TelephonyActivity(context)
            TapGate.CAMERA_VISIBILITY -> CameraVisibility(context)
            TapGate.USB_STATE -> UsbState(context, Handler(), ColumbusModule.provideTransientGateDuration())
            TapGate.APP_SHOWING -> AppVisibility(context, gate.data!!)
        })
    }
    return gates
}

fun getWakefulnessLifecycle(): LazyWakefulness {
    return LazyWakefulness(WakefulnessLifecycle())
}

fun String.splitToArray(): Array<String> {
    return if(this.contains(",")){
        this.split(",").toTypedArray()
    }else{
        arrayOf(this)
    }
}

fun GestureSensorImpl.setTfClassifier(assetManager: AssetManager, tfModel: String){
    Log.d("TAS", "setTfClassifier $tfModel")
    val tapRt = GestureSensorImpl::class.java.getDeclaredField("tap").setAccessibleR(true).get(this) as TapRT
    val tfClassifier = TfClassifier(assetManager, tfModel)
    TapRT::class.java.getDeclaredField("_tflite").setAccessibleR(true).set(tapRt, tfClassifier)
}

fun Context.isPackageCamera(packageName: String): Boolean {
    val intentActions = arrayOf(MediaStore.ACTION_IMAGE_CAPTURE, "android.media.action.STILL_IMAGE_CAMERA", "android.media.action.VIDEO_CAMERA")
    intentActions.forEach {
        if(packageManager.resolveActivity(Intent(it).setPackage(packageName), 0) != null) return true
    }
    return false
}

fun Context.isPackageAssistant(packageName: String): Boolean {
    return packageManager.resolveActivity(Intent(Intent.ACTION_VOICE_COMMAND).setPackage(packageName), 0) != null
}

/**
 * Based on [com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings]
 * @see [AccessibilityUtils](https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java.L55)
 */
fun isAccessibilityServiceEnabled(context: Context, accessibilityService: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, accessibilityService)
    val enabledServicesSetting: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString: String = colonSplitter.next()
        val enabledService: ComponentName? = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService.equals(expectedComponentName)) return true
    }
    return false
}

/*
    setAccessible, just returning the Field/Method to so it can be inlined
 */
fun Field.setAccessibleR(accessible: Boolean): Field {
    this.isAccessible = accessible
    return this
}

fun Method.setAccessibleR(accessible: Boolean): Method {
    this.isAccessible = accessible
    return this
}

/*
    Utility method to call Dependency.get(Class) from Dagger in SystemUI
 */
fun ClassLoader.getDependency(clazz: Class<*>): Any{
    val dependency = XposedHelpers.findClass("com.android.systemui.Dependency", this)
    val getMethod = dependency.getMethod("get", Class::class.java)
    return getMethod.invoke(null, clazz)
}

fun ClassLoader.doubleCheck(instance: Any): Any {
    val doubleCheck = XposedHelpers.findClass("dagger.internal.DoubleCheck", this)
    val provider = XposedHelpers.findClass("javax.inject.Provider", this)
    return doubleCheck.getConstructor(provider).newInstance(instance)
}

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun Context.getToolbarHeight(): Int {
    val tv = TypedValue()
    if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    }
    return 0
}

fun View.animateColorChange(@ColorInt beforeColor: Int? = null, @ColorInt afterColor: Int): ValueAnimator {
    val before = beforeColor ?: (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    return ValueAnimator.ofObject(ArgbEvaluator(), before, afterColor).apply {
        duration = 250
        addUpdateListener {
            this@animateColorChange.setBackgroundColor(it.animatedValue as Int)
        }
        start()
    }
}

fun View.isOverlapping(secondView: View): Boolean {
    val firstPosition = IntArray(2)
    val secondPosition = IntArray(2)

    getLocationOnScreen(firstPosition)
    secondView.getLocationOnScreen(secondPosition)

    val rectFirstView = Rect(firstPosition[0], firstPosition[1], firstPosition[0] + measuredWidth, firstPosition[1] + measuredHeight)
    val rectSecondView = Rect(secondPosition[0], secondPosition[1], secondPosition[0] + secondView.measuredWidth, secondPosition[1] + secondView.measuredHeight)
    return rectFirstView.intersect(rectSecondView)
}

fun View.clonePosition(otherView: View){
    x = otherView.x
    y = otherView.y
}

fun View.cloneSize(otherView: View){
    layoutParams.apply {
        width = otherView.measuredWidth
        height = otherView.measuredHeight
    }
    requestLayout()
}

fun View.animateBackgroundStateChange(@ColorInt beforeColor: Int? = null, @ColorInt afterColor: Int): ValueAnimator {
    val before = beforeColor ?: (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    return ValueAnimator.ofObject(ArgbEvaluator(), before, afterColor).apply {
        duration = 250
        addUpdateListener {
            this@animateBackgroundStateChange.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        start()
    }
}

fun View.animateElevationChange(afterElevation: Float): ValueAnimator {
    return ValueAnimator.ofFloat(elevation, afterElevation).apply {
        duration = 250
        addUpdateListener {
            this@animateElevationChange.elevation = it.animatedValue as Float
        }
        start()
    }
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

val Fragment.sharedPreferences
    get() = context?.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)

//Following methods based off https://code.highspec.ru/Mikanoshi/CustoMIUIzer
fun stringPrefToUri(name: String): Uri {
    return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/string/" + name)
}

fun intPrefToUri(name: String): Uri {
    return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/integer/" + name)
}

fun boolPrefToUri(name: String): Uri {
    return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/boolean/" + name)
}

fun getSharedStringPref(context: Context, name: String, defValue: String): String? {
    val uri: Uri = stringPrefToUri(name)
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        cursor.moveToFirst()
        val prefValue: String = cursor.getString(0)
        cursor.close()
        return if(prefValue.isEmpty()) defValue
        else prefValue
    } else defValue
}

fun getSharedIntPref(context: Context, name: String, defValue: Int): Int {
    val uri: Uri = intPrefToUri(name)
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        cursor.moveToFirst()
        val prefValue: Int = cursor.getInt(0)
        cursor.close()
        prefValue
    } else defValue
}

fun getSharedBoolPref(context: Context, name: String, defValue: Boolean): Boolean {
    val uri: Uri = boolPrefToUri(name)
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        cursor.moveToFirst()
        val prefValue: Int = cursor.getInt(0)
        cursor.close()
        prefValue == 1
    } else defValue
}