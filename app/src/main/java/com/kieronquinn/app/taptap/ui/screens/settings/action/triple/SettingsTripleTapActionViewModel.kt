package com.kieronquinn.app.taptap.ui.screens.settings.action.triple

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
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

class SettingsTripleTapActionViewModel(private val tapFileRepository: TapFileRepository, tapSharedPreferences: TapSharedPreferences): SettingsActionViewModel(tapSharedPreferences) {

    override val actions: LiveData<Array<ActionInternal>> = tapFileRepository.tripleTapActions.asLiveData()

    override fun navigateToAddDialog(fragment: Fragment) {
        super.navigateToAddDialog(fragment)
        fragment.navigate(SettingsTripleTapActionFragmentDirections.actionSettingsActionTripleFragmentToSettingsActionAddContainerBottomSheetFragment(
            SettingsActionAddContainerBottomSheetViewModel.ActionType.TRIPLE))
    }

    override fun addAction(action: ActionInternal) {
        tapFileRepository.saveTripleTapActions(tapFileRepository.tripleTapActions.value.plus(action))
    }

    override fun getActions() {
        tapFileRepository.getTripleTapActions()
    }

    override fun saveActions(actions: MutableList<ActionInternal>) {
        tapFileRepository.saveTripleTapActions(actions.toTypedArray())
    }

    override fun addGateForAction(whenGateInternal: WhenGateInternal, position: Int) {
        (state.value as? State.Loaded)?.actions?.let {
            it[position].whenList.add(whenGateInternal)
            saveActions(it)
        }
    }

    override fun showWhenGateFragment(fragment: Fragment, currentGates: List<TapGate>) {
        fragment.navigate(SettingsTripleTapActionFragmentDirections.actionSettingsActionTripleFragmentToSettingsGateAddContainerBottomSheetFragment(true, SettingsGateAddContainerBottomSheetViewModel.CurrentGates(currentGates)))
    }

    override fun onHeaderClicked(fragment: Fragment) {
        //Unused
    }

}