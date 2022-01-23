package com.kieronquinn.app.taptap.models.backup

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class LegacyBackup {

    class Action {
        @SerializedName("action")
        val name: String? = null
        @SerializedName("whenList")
        val whenGates: List<WhenGate>? = null
        @SerializedName("data")
        val extraData: String? = null
    }

    class Gate {
        @SerializedName("gate")
        val name: String? = null
        val isActivated: Boolean? = null
        @SerializedName("data")
        val extraData: String? = null
    }

    class WhenGate {
        @SerializedName("gate")
        val name: String? = null
        val isInverted: Boolean? = null
        @SerializedName("data")
        val extraData: String? = null
    }

    class Setting {
        var key: String? = null
        var value: String? = null
    }

    @SerializedName("double")
    val doubleTapActionsRaw: String? = null

    @SerializedName("triple")
    val tripleTapActionsRaw: String? = null

    @SerializedName("gates")
    val gatesRaw: String? = null

    val settings: List<Setting>? = null

    @SerializedName("settings_legacy")
    val legacySettings: List<Setting>? = null

    fun getDoubleTapActions(gson: Gson): Array<Action>? {
        if(doubleTapActionsRaw.isNullOrEmpty()) return null
        return gson.fromJson(doubleTapActionsRaw, Array<Action>::class.java)
    }

    fun getTripleTapActions(gson: Gson): Array<Action>? {
        if(tripleTapActionsRaw.isNullOrEmpty()) return null
        return gson.fromJson(tripleTapActionsRaw, Array<Action>::class.java)
    }

    fun getGates(gson: Gson): Array<Gate>? {
        if(gatesRaw.isNullOrEmpty()) return null
        return gson.fromJson(gatesRaw, Array<Gate>::class.java)
    }

}