package com.kieronquinn.app.taptap.ui.screens.settings.actions.tripletap

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.components.settings.invert
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.TripleTapAction
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepository
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericViewModelImpl
import com.kieronquinn.app.taptap.utils.extensions.randomId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsActionsTripleViewModel(
    context: Context,
    whenGatesRepository: WhenGatesRepository,
    actionsRepository: ActionsRepository,
    gatesRepository: GatesRepository,
    navigation: ContainerNavigation
) :
    SettingsActionsGenericViewModelImpl<TripleTapAction>(
        context,
        whenGatesRepository,
        actionsRepository,
        gatesRepository,
        navigation
    ) {

    abstract val tripleTapEnabled: TapTapSettings.TapTapSetting<Boolean>
    abstract fun onTripleTapSwitchClicked()

}

class SettingsActionsTripleViewModelImpl(
    context: Context,
    private val actionsRepository: ActionsRepository,
    private val whenGatesRepository: WhenGatesRepositoryTriple<*>,
    gatesRepository: GatesRepository,
    private val navigation: ContainerNavigation,
    settings: TapTapSettings
) : SettingsActionsTripleViewModel(
    context,
    whenGatesRepository,
    actionsRepository,
    gatesRepository,
    navigation
) {

    override val header = SettingsActionsItem.Header(R.string.help_action_triple, ::onHeaderCloseClicked)
    override val tripleTapEnabled = settings.actionsTripleTapEnabled
    private val showHeaderSetting = settings.actionsTripleTapShowHelp
    override val showHeader = showHeaderSetting.asFlow().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        settings.actionsTripleTapShowHelp.getSync()
    )

    override suspend fun getActions() = actionsRepository.getSavedTripleTapActions()
    override suspend fun getWhenGates() = whenGatesRepository.getWhenGates()

    override val switchChanged = MutableSharedFlow<Unit>()

    override fun createAction(uiAction: TapTapUIAction, index: Int): TripleTapAction {
        return TripleTapAction(
            id = randomId(),
            name = uiAction.tapAction.name,
            index = index,
            extraData = uiAction.extraData
        )
    }

    override suspend fun addActionToRepo(action: TripleTapAction): Long {
        return actionsRepository.addTripleTapAction(action)
    }

    override fun removeAction(id: Int) {
        viewModelScope.launch {
            actionsRepository.deleteTripleTapAction(id)
        }
    }

    override fun moveActionInternal(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            actionsRepository.moveTripleTapAction(fromIndex, toIndex)
        }
    }

    override suspend fun getNextActionIndex(): Int {
        return actionsRepository.getNextTripleTapActionIndex()
    }

    override fun onTripleTapSwitchClicked() {
        viewModelScope.launch {
            tripleTapEnabled.invert()
            switchChanged.emit(Unit)
        }
    }

    override fun onWhenGateChipClicked(action: TapTapUIAction) {
        viewModelScope.launch {
            navigation.navigate(
                SettingsActionsTripleFragmentDirections.actionSettingsActionsTripleFragmentToSettingsActionsWhenGatesFragment(action, true)
            )
        }
    }

    private fun onHeaderCloseClicked() {
        viewModelScope.launch {
            showHeaderSetting.set(false)
        }
    }

}