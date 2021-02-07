package com.kieronquinn.app.taptap.utils.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import net.dinglisch.android.tasker.TaskerIntent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";

fun Intent.getShortcutAsData(): String? {
    val returnedIntent = getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
    return returnedIntent?.serialize()
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

fun Intent.deserialize(jsonString: String): Intent {
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
                    parseIntentExtra(
                        key,
                        this,
                        this@deserialize
                    )
                }
            }
        }
    }
    return this
}

fun Intent.doesExist(context: Context): Boolean {
    return context.packageManager.resolveActivity(this, 0) != null
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

fun Context.isAppLaunchable(packageName: String): Boolean {
    return try{
        packageManager.getLaunchIntentForPackage(packageName) != null
    }catch (e: Exception){
        false
    }
}

fun Context.isTaskerInstalled(): Boolean {
    val packageNames = arrayOf(TaskerIntent.TASKER_PACKAGE, TaskerIntent.TASKER_PACKAGE_CUPCAKE, TaskerIntent.TASKER_PACKAGE_MARKET)
    return packageNames.any { isAppLaunchable(it) }
}

fun Context.isPackageCamera(packageName: String): Boolean {
    val intentActions = arrayOf(MediaStore.ACTION_IMAGE_CAPTURE, "android.media.action.STILL_IMAGE_CAMERA", "android.media.action.VIDEO_CAMERA")
    intentActions.forEach {
        if(packageManager.resolveActivity(Intent(it).setPackage(packageName), 0) != null) return true
    }
    return false
}

fun getCameraLaunchIntent(secure: Boolean): Intent {
    val action = if(secure) MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE
    else MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
    return Intent(action).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

fun Context.isPackageAssistant(packageName: String): Boolean {
    return packageManager.resolveActivity(Intent(Intent.ACTION_VOICE_COMMAND).setPackage(packageName), 0) != null
}