package com.kieronquinn.app.taptap.models.backup

class Backup {

    companion object {
        const val BACKUP_VERSION = 1
    }

    class Metadata {
        val backupVersion = BACKUP_VERSION
        var tapTapVersion: Int? = null
        var tapTapVersionName: String? = null
    }

    class Settings {
        var serviceEnabled: Boolean? = null
        var lowPowerMode: Boolean? = null
        var columbusCHRELowSensitivity: Boolean? = null
        var columbusSensitivityLevel: Int? = null
        var columbusCustomSensitivity: Float? = null
        var columbusTapModel: String? = null
        var reachabilityLeftHanded: Boolean? = null
        var feedbackVibrate: Boolean? = null
        var feedbackVibrateDND: Boolean? = null
        var feedbackWakeDevice: Boolean? = null
        var advancedLegacyWake: Boolean? = null
        var advancedAutoRestart: Boolean? = null
        var advancedTensorLowPower: Boolean? = null
        //Skip hasPreviouslyGrantedSui
        var actionsTripleTapEnabled: Boolean? = null
        //Skip UI options

        override fun toString(): String {
            return "Settings [serviceEnabled=$serviceEnabled, lowPowerMode=$lowPowerMode, chreLS=$columbusCHRELowSensitivity, csl=$columbusSensitivityLevel, ccs=$columbusCustomSensitivity, model=$columbusTapModel, leftHanded=$reachabilityLeftHanded, vibrate=$feedbackVibrate, dnd=$feedbackVibrateDND, wake=$feedbackWakeDevice, legacyWake=$advancedLegacyWake, restart=$advancedAutoRestart, tensorLowPower=$advancedTensorLowPower, tripleTapEnabled=$actionsTripleTapEnabled]"
        }
    }

    class Action {
        var id: Int? = null
        var name: String? = null
        var index: Int? = null
        var extraData: String? = null

        override fun toString(): String {
            return "Action [id=$id, name=$name, index=$index, extraData=$extraData]"
        }
    }

    class Gate {
        var id: Int? = null
        var name: String? = null
        var enabled: Boolean? = null
        var index: Int? = null
        var extraData: String? = null

        override fun toString(): String {
            return "Gate [id=$id, name=$name, enabled=$enabled, index=$index, extraData=$extraData]"
        }
    }

    class WhenGate {
        var id: Int? = null
        var actionId: Int? = null
        var name: String? = null
        var invert: Boolean? = null
        var index: Int? = null
        var extraData: String? = null

        override fun toString(): String {
            return "WhenGate [id=$id, actionId=$actionId, name=$name, invert=$invert, index=$index, extraData=$extraData]"
        }
    }

    var metadata: Metadata? = null
    var settings: Settings? = null
    var doubleTapActions: List<Action>? = null
    var tripleTapActions: List<Action>? = null
    var gates: List<Gate>? = null
    var whenGatesDouble: List<WhenGate>? = null
    var whenGatesTriple: List<WhenGate>? = null

}