package com.kieronquinn.app.taptap.ui.screens.settings.gate.add.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.extensions.observeOneShot
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.ui.activities.AppPickerActivity
import com.kieronquinn.app.taptap.ui.screens.picker.app.AppPickerFragment
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel

class SettingsGateAddListViewModel(private val tapFileRepository: TapFileRepository): BaseViewModel() {

    fun getAvailableGates(context: Context, tapGateCategory: TapGateCategory, currentGates: List<TapGate>?): Array<TapGate> {
        return GateInternal.getAvailableGates(context, tapGateCategory, currentGates ?: tapFileRepository.gates.value.map { it.gate }).toTypedArray()
    }

    fun onGateClicked(fragment: SettingsGateAddListFragment, sharedViewModel: SettingsGateAddContainerBottomSheetViewModel, gate: TapGate){
        if(gate.isGateDataSatisfied(fragment.requireContext())){
            sharedViewModel.addGate(gate)
        }else{
            when(val dataTypeAction = gate.getDataTypeAction(fragment, sharedViewModel)){
                is DataTypeAction.ActivityResult -> {
                    fragment.activityResultLiveData.observeOneShot(fragment.viewLifecycleOwner) {
                        dataTypeAction.callback.invoke(it)
                    }
                    fragment.activityLauncher.launch(dataTypeAction.intent)
                }
                else -> sharedViewModel.addGate(gate)
            }
        }
    }

    private fun TapGate.getDataTypeAction(fragment: SettingsGateAddListFragment, sharedViewModel: SettingsGateAddContainerBottomSheetViewModel): DataTypeAction.ActivityResult? {
        val context = fragment.requireContext()
        return when(dataType){
            GateDataTypes.PACKAGE_NAME -> {
                DataTypeAction.ActivityResult(Intent(context, AppPickerActivity::class.java)){
                    if(it?.resultCode == Activity.RESULT_OK){
                        val selectedApp = it.data?.getStringExtra(AppPickerFragment.KEY_APP) ?: return@ActivityResult
                        sharedViewModel.addGate(this, selectedApp)
                    }
                }
            }
            else -> null
        }
    }

    private sealed class DataTypeAction<T>(open val callback: (T) -> Unit) {
        data class ActivityResult(val intent: Intent, override val callback: (androidx.activity.result.ActivityResult?) -> Unit): DataTypeAction<androidx.activity.result.ActivityResult?>(callback)
        data class Permission(val requestedPermission: String, override val callback: (Boolean?) -> Unit): DataTypeAction<Boolean?>(callback)
    }

}