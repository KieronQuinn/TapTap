package com.kieronquinn.app.taptap.models.shared

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedArgument(val action: TapTapActionDirectory? = null, val gate: TapTapGateDirectory? = null): Parcelable {

    fun toBundle(): Bundle {
        return bundleOf(ARG_NAME_SHARED_ARGUMENT to this)
    }

}

const val ARG_NAME_SHARED_ARGUMENT = "argument"
