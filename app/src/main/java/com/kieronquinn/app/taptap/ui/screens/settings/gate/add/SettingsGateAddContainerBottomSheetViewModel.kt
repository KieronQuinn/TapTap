package com.kieronquinn.app.taptap.ui.screens.settings.gate.add

import android.os.Parcelable
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.utils.extensions.update
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsGateAddContainerBottomSheetViewModel: ViewModel() {

    companion object {
        const val RESULT_KEY_GATE = "gate"
        const val REQUEST_KEY_ADD_GATE = "add_gate"
    }

    var isWhenGateFlow = false
    var currentGates: List<TapGate>? = null

    val scrollPosition = MutableLiveData<Int>()
    val toolbarState = MediatorLiveData<Boolean>().apply {
        addSource(scrollPosition){
            update(it > 0)
        }
    }
    val toolbarTitle = MutableLiveData<Int>()
    val backState = MutableLiveData(BackState.CLOSE)

    enum class BackState {
        CLOSE, BACK
    }

    val gate = MutableStateFlow<GateInternal?>(null)

    fun addGate(tapGate: TapGate, data: String? = null) = viewModelScope.launch {
        gate.emit(GateInternal(tapGate, true, data))
    }

    fun clearGate() = viewModelScope.launch {
        gate.emit(null)
    }

    fun setResultGate(fragment: BottomSheetDialogFragment, gate: GateInternal){
        fragment.setFragmentResult(REQUEST_KEY_ADD_GATE, bundleOf(RESULT_KEY_GATE to gate))
        fragment.dismiss()
    }

    @Parcelize
    data class CurrentGates(val gates: List<TapGate>): Parcelable
    
}