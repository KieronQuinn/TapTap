package com.kieronquinn.app.taptap.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.ContentResolver
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
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.ArraySet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.google.android.systemui.columbus.ColumbusModule
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.gates.ChargingState
import com.google.android.systemui.columbus.gates.Gate
import com.google.android.systemui.columbus.gates.PowerState
import com.google.android.systemui.columbus.gates.UsbState
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.PeakDetector
import com.google.android.systemui.columbus.sensors.TapRT
import com.google.android.systemui.columbus.sensors.TfClassifier
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.columbus.gates.*
import com.kieronquinn.app.taptap.models.GateDataTypes
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.store.GateListFile
import com.kieronquinn.app.taptap.providers.SharedPrefsProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Field
import java.lang.reflect.Method

const val SHARED_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}_prefs"
const val SHARED_PREFERENCES_KEY_MAIN_SWITCH = "main_enabled"
const val SHARED_PREFERENCES_KEY_ACTION = "action"
const val SHARED_PREFERENCES_KEY_ACTIONS_TIME = "actions_time"
const val SHARED_PREFERENCES_KEY_GATES_TIME = "gates_time"
const val SHARED_PREFERENCES_KEY_GATES = "gates"
const val SHARED_PREFERENCES_KEY_MODEL = "model"
const val SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE = "feedback_vibrate"
const val SHARED_PREFERENCES_KEY_FEEDBACK_WAKE = "feedback_wake"
const val SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND = "feedback_override_dnd"
const val SHARED_PREFERENCES_KEY_SPLIT_SERVICE = "advanced_split_service"
const val SHARED_PREFERENCES_KEY_RESTART_SERVICE = "advanced_restart_service"

const val SHARED_PREFERENCES_KEY_SENSITIVITY = "sensitivity"

val SHARED_PREFERENCES_FEEDBACK_KEYS = arrayOf(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE)

/*
    EXPERIMENTAL: SENSITIVITY
    These values get applied to the model's noise reduction. The higher the value, the more reduction of 'noise', and therefore the harder the gesture is to run.
    Anything from 0.0 to 0.1 should really work, but 0.75 is pretty hard to trigger so that's set to the maximum and values filled in from there
    For > 0.05f, the values were initially even spaced, but that put too much weight on the higher values which made the force difference between 0.05 (default) the next value too great
    Instead I made up some values that are semi-evenly spaced and seem to provide a decent weighting
    For < 0.05f, the values are evenly spaced down to 0 which is no noise removal at all and really easy to trigger.
 */
val SENSITIVITY_VALUES = arrayOf(0.75f, 0.53f, 0.40f, 0.25f, 0.1f, 0.05f, 0.04f, 0.03f, 0.02f, 0.01f, 0.0f)

val DEFAULT_GATES = arrayOf(TapGate.POWER_STATE, TapGate.TELEPHONY_ACTIVITY)
val ALL_NON_CONFIG_GATES = arrayOf(TapGate.POWER_STATE, TapGate.POWER_STATE_INVERSE, TapGate.USB_STATE, TapGate.TELEPHONY_ACTIVITY, TapGate.CHARGING_STATE)
val CONFIGURABLE_GATES = arrayOf(TapGate.APP_SHOWING)

val DEFAULT_ACTIONS = if(TapAction.SCREENSHOT.isAvailable){
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.SCREENSHOT)
}else{
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.HOME)
}

val Context.isSplitService: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_SPLIT_SERVICE, false)

val Context.isMainEnabled: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, true)

val Context.isRestartEnabled: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_RESTART_SERVICE, false)

fun InputStream.copyFile(out: OutputStream) {
    val buffer = ByteArray(1024)
    var read: Int
    while (this.read(buffer).also { read = it } != -1) {
        out.write(buffer, 0, read)
    }
}

public fun <T> Array<out T>.indexOfOrNull(element: T): Int? {
    if(!contains(element)) return null
    return indexOf(element)
}

fun settingsGlobalGetIntOrNull(contentResolver: ContentResolver, key: String): Int? {
    return try {
        Settings.Global.getInt(contentResolver, key)
    }catch (e: Settings.SettingNotFoundException){
        null
    }
}

//Replaces hidden API
val ApplicationInfo.isSystemApp: Boolean
    get() {
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (flags and mask) != 0;
    }

fun Context.isAppLaunchable(packageName: String): Boolean {
    return try{
        packageManager.getLaunchIntentForPackage(packageName) != null
    }catch (e: Exception){
        false
    }
}

fun Intent.serialize(): String? {
    //Serialize the Intent to a JSON object storing its data as much as we can (only basic data is currently supported)
    val json = JSONObject()
    action?.let {
        json.put("action", it)
    }
    dataString?.let {
        json.put("data", it)
    }
    val jsonCategories = JSONArray()
    categories?.forEach {
        jsonCategories.put(it)
        json.put("categories", categories)
    }
    component?.let {
        json.put("component", it?.flattenToString())
    }
    flags?.let {
        json.put("flags", it)
    }
    val extras = JSONObject().apply {
        extras?.keySet()?.forEach {
            val extra = extras?.get(it)
            if(extra is String || extra is Int || extra is Float || extra is Long || extra is Double || extra is Boolean){
                put(it, JSONObject().apply {
                    put("type", "string")
                    put("value", extra)
                })
            }else if(extra is Int){
                put(it, JSONObject().apply {
                    put("type", "int")
                    put("value", extra)
                })
            }else if(extra is Float){
                put(it, JSONObject().apply {
                    put("type", "float")
                    put("value", extra.toString())
                })
            }else if(extra is Long){
                put(it, JSONObject().apply {
                    put("type", "long")
                    put("value", extra)
                })
            }else if(extra is Double){
                put(it, JSONObject().apply {
                    put("type", "double")
                    put("value", extra)
                })
            }else if(extra is Boolean){
                put(it, JSONObject().apply {
                    put("type", "boolean")
                    put("value", extra)
                })
            }else{
                Log.d("TapTap", "$it is unsupported of type ${extra.toString()}")
                //Unsupported value
                return null
            }
        }
    }
    json.put("extras", extras)
    return json.toString()
}

fun Intent.deserialize(jsonString: String){
    //Deserialize an intent from a JSON string after the above was used to serialize it
    val json = JSONObject(jsonString)
    action = json.getStringOpt("action")
    val dataString = json.getStringOpt("data")
    dataString?.let {
        data = Uri.parse(it)
    }
    if(json.has("categories")){
        try {
            json.getJSONArray("categories").run {
                for (x in 0 until length()) {
                    addCategory(getString(x))
                }
            }
        }catch (e: JSONException){
            //Not an array
            addCategory(json.getString("categories"))
        }
    }
    val componentFlattened = json.getStringOpt("component")
    componentFlattened?.let {
        component = ComponentName.unflattenFromString(it)
    }
    flags = json.getIntOpt("flags") ?: 0
    if(json.has("extras")){
        json.getJSONObject("extras").run{
            for (key in keys()) {
                getJSONObject(key).run {
                    parseIntentExtra(key, this, this@deserialize)
                }
            }
        }
    }
}

private fun parseIntentExtra(key: String, jsonObject: JSONObject, intent: Intent){
    when(jsonObject.getString("type")){
        "string" -> intent.putExtra(key, jsonObject.getString("value"))
        "int" -> intent.putExtra(key, jsonObject.getInt("value"))
        "float" -> intent.putExtra(key, jsonObject.getString("value").toFloat())
        "long" -> intent.putExtra(key, jsonObject.getLong("value"))
        "double" -> intent.putExtra(key, jsonObject.getDouble("value"))
        "boolean" -> intent.putExtra(key, jsonObject.getBoolean("value"))
    }
}

fun JSONObject.getStringOpt(key: String): String? {
    return if(has(key)) getString(key)
    else null
}

fun JSONObject.getIntOpt(key: String): Int? {
    return if(has(key)) getInt(key)
    else null
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

fun minSdk(api: Int): Boolean {
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
        gates.add(getGate(context, gate.gate, gate.data) ?: continue)
    }
    return gates
}

fun String.formatChangelog(): String {
    return this.replace("\n", "<br>")
}

fun requireBackgroundThread(){
    if(Looper.myLooper() == Looper.getMainLooper()) throw RuntimeException("Not background thread")
}

fun getSpannedText(text: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(text)
    }
}

fun runOnUiThread(run: () -> Unit){
    Handler(Looper.getMainLooper()).post { run.invoke() }
}

fun getGate(context: Context, tapGate: TapGate?, data: String?): Gate? {
    tapGate ?: return null
    return when (tapGate) {
        TapGate.POWER_STATE -> PowerState(context, wakefulnessLifecycle)
        TapGate.POWER_STATE_INVERSE -> PowerStateInverse(context)
        TapGate.CHARGING_STATE -> ChargingState(context, Handler(), ColumbusModule.provideTransientGateDuration())
        TapGate.TELEPHONY_ACTIVITY -> TelephonyActivity(context)
        TapGate.CAMERA_VISIBILITY -> CameraVisibility(context)
        TapGate.USB_STATE -> UsbState(context, Handler(), ColumbusModule.provideTransientGateDuration())
        TapGate.APP_SHOWING -> AppVisibility(context, data!!)
        TapGate.KEYBOARD_VISIBILITY -> KeyboardVisibility(context)
        TapGate.ORIENTATION_LANDSCAPE -> Orientation(context, Configuration.ORIENTATION_LANDSCAPE)
        TapGate.ORIENTATION_PORTRAIT -> Orientation(context, Configuration.ORIENTATION_PORTRAIT)
        TapGate.TABLE -> TableDetection(context)
        TapGate.POCKET -> PocketDetection(context)
        TapGate.HEADSET -> Headset(context)
        TapGate.HEADSET_INVERSE -> HeadsetInverse(context)
        TapGate.MUSIC -> Music(context)
        TapGate.MUSIC_INVERSE -> MusicInverse(context)
    }
}

fun RecyclerView.ViewHolder.adapterPositionAdjusted(hasHeader: Boolean = true): Int {
    return adapterPosition - 1
}

fun getFormattedDataForGate(context: Context, gate: TapGate, data: String?): CharSequence? {
    return when(gate.dataType){
        GateDataTypes.PACKAGE_NAME -> {
            val applicationInfo = context.packageManager.getApplicationInfo(data, 0)
            applicationInfo.loadLabel(context.packageManager)
        }
        else -> null
    } ?: return null
}

val wakefulnessLifecycle by lazy {
    LazyWakefulness(WakefulnessLifecycle())
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

fun GestureSensorImpl.getTapRT(): TapRT {
    return GestureSensorImpl::class.java.getDeclaredField("tap").setAccessibleR(true).get(this) as TapRT
}

fun PeakDetector.getMinNoiseToTolerate(): Float {
    return PeakDetector::class.java.getDeclaredField("_minNoiseTolerate").setAccessibleR(true).getFloat(this)
}

fun Context.isPackageCamera(packageName: String): Boolean {
    val intentActions = arrayOf(MediaStore.ACTION_IMAGE_CAPTURE, "android.media.action.STILL_IMAGE_CAMERA", "android.media.action.VIDEO_CAMERA")
    intentActions.forEach {
        if(packageManager.resolveActivity(Intent(it).setPackage(packageName), 0) != null) return true
    }
    return false
}

fun Context.getCameraLaunchIntent(): ArrayList<Intent> {
    val intentActions = arrayOf(MediaStore.ACTION_IMAGE_CAPTURE, "android.media.action.STILL_IMAGE_CAMERA", "android.media.action.VIDEO_CAMERA")
    val packages = ArrayList<Intent>()
    intentActions.forEach {
        packages.addAll(packageManager.queryIntentActivities(Intent(it), 0).mapNotNull { activity ->
            if(packages.any { intent -> intent.`package` == activity.activityInfo.packageName }) null
            else packageManager.getLaunchIntentForPackage(activity.activityInfo.packageName)?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    return packages
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

val Context.sharedPreferences
    get() = getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)

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