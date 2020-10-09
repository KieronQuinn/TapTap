package com.kieronquinn.app.taptap.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.util.ArraySet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.RecyclerView
import com.android.systemui.keyguard.WakefulnessLifecycle
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
import com.kieronquinn.app.taptap.preferences.Preference
import com.kieronquinn.app.taptap.preferences.SliderPreference
import com.kieronquinn.app.taptap.providers.SharedPrefsProvider
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.services.TapColumbusService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.ceil
import kotlin.collections.MutableSet
import kotlin.math.sqrt

const val SHARED_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}_prefs"
const val SHARED_PREFERENCES_KEY_MAIN_SWITCH = "main_enabled"
const val SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH = "triple_tap_enabled"
const val SHARED_PREFERENCES_KEY_ACTIONS_TIME = "actions_time"
const val SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME = "actions_triple_time"
const val SHARED_PREFERENCES_KEY_GATES = "gates"
const val SHARED_PREFERENCES_KEY_MODEL = "model"
const val SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE = "feedback_vibrate"
const val SHARED_PREFERENCES_KEY_FEEDBACK_WAKE = "feedback_wake"
const val SHARED_PREFERENCES_KEY_FEEDBACK_OVERRIDE_DND = "feedback_override_dnd"
const val SHARED_PREFERENCES_KEY_SPLIT_SERVICE = "advanced_split_service"
const val SHARED_PREFERENCES_KEY_RESTART_SERVICE = "advanced_restart_service"
const val SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP = "has_seen_setup"

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

val GESTURE_REQUIRING_ACTIONS = arrayOf(TapAction.HAMBURGER)

val DEFAULT_ACTIONS = if(TapAction.SCREENSHOT.isAvailable){
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.SCREENSHOT)
}else{
    arrayOf(TapAction.LAUNCH_ASSISTANT, TapAction.HOME)
}

val DEFAULT_ACTIONS_TRIPLE = arrayOf(TapAction.NOTIFICATIONS)

val Context.isSplitService: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_SPLIT_SERVICE, true)

val Context.isMainEnabled: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, true)

val Context.isTripleTapEnabled: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH, false)

val Context.isRestartEnabled: Boolean
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_RESTART_SERVICE, false)

val Context.hasSeenSetup: Boolean
    get() {
        if(sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP, false)) return true
        if(sharedPreferences.contains(SHARED_PREFERENCES_KEY_ACTIONS_TIME)) return true
        if(sharedPreferences.contains(SHARED_PREFERENCES_KEY_ACTIONS_TRIPLE_TIME)) return true
        return false
    }

fun InputStream.copyFile(out: OutputStream) {
    val buffer = ByteArray(1024)
    var read: Int
    while (this.read(buffer).also { read = it } != -1) {
        out.write(buffer, 0, read)
    }
}

fun getVibrationEffect(effectId: Int): VibrationEffect? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        VibrationEffect.createPredefined(effectId)
    } else null
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

fun getSystemProperty(key: String): String {
    val systemProperties = Class.forName("android.os.SystemProperties")
    return systemProperties.getMethod("get", String::class.java).invoke(null, key) as String
}

val isMiui: Boolean
    get() = getSystemProperty("ro.miui.ui.version.code").isNotEmpty()

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

fun isAudioStreamActive(audioStream: Int): Boolean {
    runCatching {
        val clazz = Class.forName("android.media.AudioSystem")
        val isStreamActive = clazz.getDeclaredMethod("isStreamActive", Integer.TYPE, Integer.TYPE)
        return isStreamActive.invoke(null, audioStream, 0) as Boolean
    }
    return false
}

val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

//This is for the normal size ONLY and should NEVER be used when windowInsets are available
fun getStaticStatusBarHeight(context: Context): Int {
    val resources: Resources = context.resources
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else ceil(
        ((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 24 else 25) * resources.displayMetrics.density).toDouble()
    ).toInt()
}

fun ColumbusService.setActions(list: List<Action>){
    (this as TapColumbusService).setActions(list)
}

fun ColumbusService.setActionsTriple(list: List<Action>){
    this as TapColumbusService
    tripleTapActions.apply {
        clear()
        addAll(list)
    }
}

fun ColumbusService.setFeedback(set: Set<FeedbackEffect>){
    this.effects = set
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
    updateSensorListener()
}

fun View.fadeIn(callback: (() -> Unit)? = null) {

    //Don't run animation if the view is already visible
    if (visibility == View.VISIBLE && alpha == 1f)
        return

    val fadeInAnimation = AlphaAnimation(0f, 1f)
    fadeInAnimation.duration = 500
    fadeInAnimation.fillAfter = true
    fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            callback?.invoke()
        }
    })
    visibility = View.VISIBLE
    startAnimation(fadeInAnimation)
}

fun View.fadeOut(endVisibility: Int = View.INVISIBLE, callback: (() -> Unit)? = null) {

    //Don't run animation if the view is already invisible
    if (visibility != View.VISIBLE)
        return

    val fadeOutAnimation = AlphaAnimation(1f, 0f)
    fadeOutAnimation.duration = 500
    fadeOutAnimation.fillAfter = true
    fadeOutAnimation.fillBefore = false
    fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            callback?.invoke()
        }

        override fun onAnimationStart(animation: Animation?) {
            visibility = endVisibility
        }

    })
    startAnimation(fadeOutAnimation)
}

fun View.animateBackgroundTint(@ColorInt toColor: Int){
    val fromColor = backgroundTintList?.defaultColor ?: Color.TRANSPARENT
    ValueAnimator().apply {
        setIntValues(fromColor, toColor)
        setEvaluator(ArgbEvaluator())
        addUpdateListener {
            this@animateBackgroundTint.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        duration = 1000L
    }.start()
}

val View.centerX
    get() = x + (measuredWidth / 2)

val View.centerY
    get() = y + (measuredHeight / 2)

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
    stopListening()
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
        TapGate.LOCK_SCREEN -> LockScreenState(context)
        TapGate.LOCK_SCREEN_INVERSE -> LockScreenStateInverse(context)
        TapGate.CHARGING_STATE -> ChargingState(context, Handler(), 500L)
        TapGate.TELEPHONY_ACTIVITY -> TelephonyActivity(context)
        TapGate.CAMERA_VISIBILITY -> CameraVisibility(context)
        TapGate.USB_STATE -> UsbState(context, Handler(), 500L)
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
        TapGate.ALARM -> Alarm(context)
    }
}

fun RecyclerView.ViewHolder.adapterPositionAdjusted(hasHeader: Boolean = true): Int {
    return adapterPosition - 1
}

fun getFormattedDataForGate(context: Context, gate: TapGate, data: String?): CharSequence? {
    return when(gate.dataType){
        GateDataTypes.PACKAGE_NAME -> {
            val applicationInfo = context.packageManager.getApplicationInfo(data!!, 0)
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
    val tfClassifier = TfClassifier(assetManager, tfModel)
    tap._tflite = tfClassifier
}

fun GestureSensorImpl.getTapRT(): TapRT {
    return tap
}

fun PeakDetector.getMinNoiseToTolerate(): Float {
    return _minNoiseTolerate
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

@ColorRes
fun Context.resolveColorAttribute(@AttrRes res: Int): Int {
    val tv = TypedValue()
    if (theme.resolveAttribute(res, tv, true)) {
        return tv.resourceId
    }
    return 0
}

fun Context.unregisterReceiverOpt(receiver: BroadcastReceiver){
    try {
        unregisterReceiver(receiver)
    }catch (e: IllegalArgumentException){
        //Do nothing, already unregistered
    }
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

fun Context.getPhysicalScreenSize(): Double {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    manager.defaultDisplay.getMetrics(displayMetrics)
    val widthInInches : Float = displayMetrics.widthPixels / displayMetrics.xdpi
    val heightInInches : Float = displayMetrics.heightPixels / displayMetrics.ydpi
    val ab = (widthInInches * widthInInches) + (heightInInches * heightInInches).toDouble()
    return sqrt(ab)
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

fun PreferenceFragmentCompat.getPreference(key: String, invoke: (Preference) -> Unit) {
    findPreference<Preference>(key)?.run(invoke)
}

fun PreferenceFragmentCompat.getSwitchPreference(key: String, invoke: (SwitchPreference) -> Unit) {
    findPreference<SwitchPreference>(key)?.run(invoke)
}

fun PreferenceFragmentCompat.getSliderPreference(key: String, invoke: (SliderPreference) -> Unit) {
    findPreference<SliderPreference>(key)?.run(invoke)
}