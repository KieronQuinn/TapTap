package com.kieronquinn.app.taptap.models.store

import android.content.Context
import android.content.SharedPreferences
import com.google.android.systemui.columbus.gates.Gate
import com.google.gson.Gson
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.utils.*
import java.io.File
import java.nio.charset.Charset

object GateListFile {

    private val defaultGates by lazy {
        ALL_NON_CONFIG_GATES.map { createGateInternalForGate(it, DEFAULT_GATES.contains(it)) }.toTypedArray()
    }

    private fun getGateListFile(context: Context): File {
        return File(context.filesDir, "gates.json")
    }

    fun loadFromFile(context: Context): Array<GateInternal> {
        val file = getGateListFile(context)
        if(!file.exists()) return defaultGates
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return defaultGates
        return Gson().fromJson(fileData, Array<GateInternal>::class.java)
    }

    private fun createGateInternalForGate(gate: TapGate, isActivated: Boolean): GateInternal {
        return GateInternal(gate, isActivated)
    }

    fun saveToFile(context: Context, actions: Array<GateInternal>, sharedPreferences: SharedPreferences?){
        val file = getGateListFile(context)
        val json = Gson().toJson(actions)
        file.writeText(json, Charset.defaultCharset())
        sharedPreferences?.run {
            //Put the current time in the shared prefs as a bit of a hacky way to trigger an update on the accessibility service
            edit().putLong(SHARED_PREFERENCES_KEY_GATES, System.currentTimeMillis()).apply()
        }
    }

}