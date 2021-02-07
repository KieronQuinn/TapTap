package com.kieronquinn.app.taptap.ui.screens.settings.action.double

import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.ui.screens.settings.action.SettingsActionViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel

class SettingsDoubleTapActionViewModel(private val tapFileRepository: TapFileRepository, tapSharedPreferences: TapSharedPreferences): SettingsActionViewModel(tapSharedPreferences) {

    override val actions = tapFileRepository.doubleTapActions.asLiveData()

    override fun navigateToAddDialog(fragment: Fragment) {
        super.navigateToAddDialog(fragment)
        fragment.navigate(SettingsDoubleTapActionFragmentDirections.actionSettingsActionFragmentToSettingsActionAddContainerBottomSheetFragment(
            SettingsActionAddContainerBottomSheetViewModel.ActionType.DOUBLE))
    }

    override fun addAction(action: ActionInternal) {
        tapFileRepository.saveDoubleTapActions(tapFileRepository.doubleTapActions.value.plus(action))
    }

    override fun getActions(){
        tapFileRepository.getDoubleTapActions()
    }

    override fun saveActions(actions: MutableList<ActionInternal>) {
        tapFileRepository.saveDoubleTapActions(actions.toTypedArray())
    }

    override fun addGateForAction(whenGateInternal: WhenGateInternal, position: Int) {
        (state.value as? State.Loaded)?.actions?.let {
            it[position].whenList.add(whenGateInternal)
            saveActions(it)
        }
    }

    override fun showWhenGateFragment(fragment: Fragment, currentGates: List<TapGate>) {
        fragment.navigate(SettingsDoubleTapActionFragmentDirections.actionSettingsActionFragmentToSettingsGateAddContainerBottomSheetFragment(true, SettingsGateAddContainerBottomSheetViewModel.CurrentGates(currentGates)))
    }

    override fun onHeaderClicked(fragment: Fragment) {
        fragment.navigate(SettingsDoubleTapActionFragmentDirections.actionSettingsActionFragmentToSettingsDoubleTapActionBottomSheetFragment())
    }

}