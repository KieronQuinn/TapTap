package com.kieronquinn.app.taptap.ui.screens.settings.gate

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel

class SettingsGateViewModel(private val tapFileRepository: TapFileRepository): BaseViewModel() {

    private val gates = tapFileRepository.gates.asLiveData()

    val state = MediatorLiveData<State>().apply {
        addSource(gates){
            with(gates.value){
                when {
                    this == null -> {
                        update(State.Loading)
                    }
                    this.isEmpty() -> {
                        update(State.Empty)
                    }
                    else -> {
                        update(State.Loaded(this.toMutableList()))
                    }
                }
            }
        }
    }

    val selectedState = MutableLiveData<SelectedState>(SelectedState.None())

    val fabState = MediatorLiveData<Boolean>().apply {
        addSource(selectedState){
            update(it is SelectedState.Selected)
        }
    }

    fun getGates(){
        tapFileRepository.getGates()
    }

    fun setSelectedIndex(adapterPosition: Int) {
        with(selectedState.value) {
            when (this) {
                is SelectedState.Selected -> {
                    if (selectedItemIndex == adapterPosition || adapterPosition == -1) {
                        selectedState.update(SelectedState.None(selectedItemIndex))
                    }else{
                        selectedState.update(SelectedState.Selected(adapterPosition, selectedItemIndex))
                    }
                }
                is SelectedState.None -> {
                    selectedState.update(SelectedState.Selected(adapterPosition))
                }
            }
        }
    }

    fun getSelectedIndex(): Int {
        return with(selectedState.value) {
            when(this) {
                is SelectedState.Selected -> this.selectedItemIndex
                else -> -1
            }
        }
    }

    fun onFabClicked(fragment: Fragment, adapter: SettingsGateAdapter) = with(selectedState.value) {
        when(this){
            is SelectedState.None -> {
                navigateToAddDialog(fragment)
            }
            is SelectedState.Selected -> {
                (state.value as? State.Loaded)?.gates?.removeAt(this.selectedItemIndex - 1)
                adapter.notifyItemRemoved(this.selectedItemIndex)
                commitChanges()
                selectedState.update(SelectedState.None())
            }
            else -> {}
        }
    }

    private fun navigateToAddDialog(fragment: Fragment) {
        fragment.setFragmentResultListener(SettingsGateAddContainerBottomSheetViewModel.REQUEST_KEY_ADD_GATE){ requestKey, bundle ->
            if(requestKey == SettingsGateAddContainerBottomSheetViewModel.REQUEST_KEY_ADD_GATE){
                val result = bundle.getParcelable<GateInternal>(
                    SettingsGateAddContainerBottomSheetViewModel.RESULT_KEY_GATE
                ) ?: return@setFragmentResultListener
                addGate(result)
            }
        }
        fragment.navigate(SettingsGateFragmentDirections.actionSettingsGateFragmentToSettingsGateAddContainerBottomSheetFragment(false, null))
    }

    private fun addGate(gate: GateInternal){
        tapFileRepository.saveGates(tapFileRepository.gates.value.plus(gate))
    }

    private fun commitChanges(){
        (state.value as? State.Loaded)?.gates?.let {
            tapFileRepository.saveGates(it.toTypedArray())
        }
    }

    fun onItemCheckClicked(item: GateInternal){
        item.isActivated = !item.isActivated
        commitChanges()
    }

    fun onHeaderClick(fragment: Fragment){
        fragment.navigate(SettingsGateFragmentDirections.actionSettingsGateFragmentToSettingsGateBottomSheetFragment())
    }

    sealed class State {
        object Loading: State()
        object Empty: State()
        data class Loaded(val gates: MutableList<GateInternal>): State()
    }

    sealed class SelectedState(open val previousSelectedIndex: Int = -1) {
        data class None(override val previousSelectedIndex: Int = -1): SelectedState(previousSelectedIndex)
        data class Selected(val selectedItemIndex: Int, override val previousSelectedIndex: Int = -1): SelectedState(previousSelectedIndex)
    }

    override fun onBackPressed(fragment: Fragment): Boolean {
        return if(selectedState.value is SelectedState.Selected){
            selectedState.update(SelectedState.None((selectedState.value as SelectedState.Selected).selectedItemIndex))
            true
        }else {
            super.onBackPressed(fragment)
        }
    }

}