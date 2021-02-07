package com.kieronquinn.app.taptap.models

import android.content.Context
import android.os.Parcelable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.getApplicationInfoOrNull
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhenGateInternal(val gate: TapGate, val isInverted: Boolean = true, var data: String? = null) : Parcelable {

    fun getChipText(context: Context): String {
        return if(gate.formattableDescription != null && gate.dataType != null){
            val gateTitle = context.getString(gate.nameRes)
            val formattedText = getFormattedDataForGate(context, gate, data)
            "$gateTitle ($formattedText)"
        }else context.getString(gate.nameRes)
    }

    fun toGateInternal(): GateInternal {
        return GateInternal(gate, true, data)
    }

    private fun getFormattedDataForGate(context: Context, gate: TapGate, data: String?): CharSequence? {
        return when(gate.dataType){
            GateDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfoOrNull(data)
                applicationInfo?.loadLabel(context.packageManager) ?: context.getString(R.string.item_action_app_uninstalled, data)
            }
            else -> null
        } ?: return null
    }

}