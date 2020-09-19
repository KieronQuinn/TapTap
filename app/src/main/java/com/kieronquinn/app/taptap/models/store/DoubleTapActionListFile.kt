package com.kieronquinn.app.taptap.models.store

import android.content.Context
import android.content.SharedPreferences
import com.google.android.systemui.columbus.gates.Gate
import com.google.gson.Gson
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.utils.DEFAULT_ACTIONS
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_ACTIONS_TIME
import java.io.File
import java.nio.charset.Charset

object DoubleTapActionListFile {

    private fun getActionListFile(context: Context): File {
        return File(context.filesDir, "actions.json")
    }

    fun loadFromFile(context: Context): Array<ActionInternal> {
        val file = getActionListFile(context)
        if(!file.exists()) return DEFAULT_ACTIONS.map { createActionInternalForAction(it) }.toTypedArray()
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return DEFAULT_ACTIONS.map { createActionInternalForAction(it) }.toTypedArray()
        return Gson().fromJson(fileData, Array<ActionInternal>::class.java)
    }

    private fun createActionInternalForAction(action: TapAction): ActionInternal {
        return ActionInternal(action, ArrayList())
    }

    fun saveToFile(context: Context, actions: Array<ActionInternal>, sharedPreferences: SharedPreferences?){
        val file = getActionListFile(context)
        val json = Gson().toJson(actions)
        file.writeText(json, Charset.defaultCharset())
        sharedPreferences?.run {
            //Put the current time in the shared prefs as a bit of a hacky way to trigger an update on the accessibility service
            edit().putLong(SHARED_PREFERENCES_KEY_ACTIONS_TIME, System.currentTimeMillis()).apply()
        }
    }

}