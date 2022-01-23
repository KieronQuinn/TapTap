package com.kieronquinn.app.taptap.utils.extensions

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Context.broadcastReceiverAsFlow(vararg actions: String) = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(intent)
        }
    }
    actions.forEach {
        registerReceiver(receiver, IntentFilter(it))
    }
    awaitClose {
        unregisterReceiver(receiver)
    }
}

//Safe to use getRunningServices for our own service
@Suppress("deprecation")
fun Context.isServiceRunning(serviceClass: Class<out Service>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.getRunningServices(Integer.MAX_VALUE).any {
        it?.service?.className == serviceClass.name
    }
}

//Safe to use getRunningTasks for our own task
@Suppress("deprecation")
fun Context.isActivityRunning(activityClass: Class<out Activity>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.getRunningTasks(Integer.MAX_VALUE).any {
        it?.baseActivity?.className == activityClass.canonicalName
    }
}

fun Context.getAccessibilityIntent(accessibilityService: Class<out AccessibilityService>): Intent {
    return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val bundle = Bundle()
        val componentName = ComponentName(packageName, accessibilityService.name).flattenToString()
        bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
        putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
        putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
    }
}

fun Context.unregisterReceiverIfRegistered(receiver: BroadcastReceiver) {
    try {
        unregisterReceiver(receiver)
    } catch (e: IllegalArgumentException) {
        //Do nothing, not registered
    }
}

val Context.isDarkMode: Boolean
    get() {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

fun Context.getPlayStoreIntentForPackage(packageName: String, fallbackUrl: String): Intent? {
    val playIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("market://details?id=$packageName")
    }
    if (packageManager.resolveActivity(playIntent, 0) != null) return playIntent
    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(fallbackUrl)
    }
    if (packageManager.resolveActivity(fallbackIntent, 0) != null) return fallbackIntent
    return null
}

fun Context.isPowerConnected(): Boolean {
    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: return false
    return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
}

/**
 *  This is a hack. `getInputMethodWindowVisibleHeight` is not meant to be exposed to apps,
 *  and is marked to be looked at and removed in the future, in which case we'll fall back
 *  on checking the foreground app instead. Catching in this method will prevent a crash
 *  if the method is gone in the future.
 */
fun Context.isKeyboardOpen(): Boolean {
    val inputService = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val height = try {
        inputService::class.java.getDeclaredMethod("getInputMethodWindowVisibleHeight").apply {
            isAccessible = true
        }.invoke(inputService) as Int
    }catch(e: Exception) {
        0
    }
    return height > 0
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val capabilities = arrayOf(
        NetworkCapabilities.TRANSPORT_BLUETOOTH,
        NetworkCapabilities.TRANSPORT_CELLULAR,
        NetworkCapabilities.TRANSPORT_ETHERNET,
        NetworkCapabilities.TRANSPORT_LOWPAN,
        NetworkCapabilities.TRANSPORT_USB,
        NetworkCapabilities.TRANSPORT_VPN,
        NetworkCapabilities.TRANSPORT_WIFI,
        NetworkCapabilities.TRANSPORT_WIFI_AWARE
    )
    return capabilities.any { networkCapabilities?.hasTransport(it) ?: false }
}

val Context.actionBarSize
    get() = TypedValue().run {
        theme.resolveAttribute(android.R.attr.actionBarSize, this, true)
        TypedValue.complexToDimensionPixelSize(data, resources.displayMetrics)
    }

@ColorInt
fun Context.themeColor(@AttrRes attrRes: Int): Int = TypedValue()
    .apply { theme.resolveAttribute (attrRes, this, true) }
    .data

/**
 * Create a formatted CharSequence from a string resource containing arguments and HTML formatting
 *
 * The string resource must be wrapped in a CDATA section so that the HTML formatting is conserved.
 *
 * Example of an HTML formatted string resource:
 * <string name="html_formatted"><![CDATA[ bold text: <B>%1$s</B> ]]></string>
 */
fun Context.getText(@StringRes id: Int, vararg args: Any?): CharSequence =
    HtmlCompat.fromHtml(String.format(getString(id), *args), HtmlCompat.FROM_HTML_MODE_COMPACT)

fun Context.deviceHasGyroscope(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)
}

fun Context.doesPackageHavePermission(packageName: String, permission: String): Boolean {
    return packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions.contains(permission)
}

