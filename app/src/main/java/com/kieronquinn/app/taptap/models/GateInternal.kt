package com.kieronquinn.app.taptap.models

import android.content.Context
import android.os.Parcelable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.getApplicationInfoOrNull
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GateInternal(val gate: TapGate, var isActivated: Boolean, var data: String? = null) : Parcelable {

    companion object {
        fun getAvailableGates(context: Context, category: TapGateCategory, currentGates: List<TapGate>): List<TapGate> {
            return TapGate.values().filter { it.category == category && (it.dataType != null || !currentGates.contains(it)) && it.isSupported(context) }
        }
    }

    fun getCardDescription(context: Context): CharSequence {
        return when(gate.dataType){
            GateDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfoOrNull(data)
                val formattedData = applicationInfo?.loadLabel(context.packageManager) ?: context.getString(R.string.item_action_app_uninstalled, data)
                context.getString(gate.formattableDescription!!, formattedData)
            }
            else -> context.getText(gate.descriptionRes)
        }
    }

    fun toWhenGate(): WhenGateInternal {
        return WhenGateInternal(gate, false, data)
    }

}