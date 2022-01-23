package com.kieronquinn.app.taptap.ui.screens.settings.actions.doubletap

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.DoubleTapAction
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepository
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericViewModelImpl
import com.kieronquinn.app.taptap.utils.extensions.randomId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsActionsDoubleViewModel(
    context: Context,
    whenGatesRepository: WhenGatesRepository,
    actionsRepository: ActionsRepository,
    gatesRepository: GatesRepository,
    navigation: ContainerNavigation
) :
    SettingsActionsGenericViewModelImpl<DoubleTapAction>(
        context,
        whenGatesRepository,
        actionsRepository,
        gatesRepository,
        navigation
    )

class SettingsActionsDoubleViewModelImpl(
    context: Context,
    private val actionsRepository: ActionsRepository,
    gatesRepository: GatesRepository,
    private val whenGatesRepository: WhenGatesRepositoryDouble<*>,
    private val navigation: ContainerNavigation,
    settings: TapTapSettings
) : SettingsActionsDoubleViewModel(
    context,
    whenGatesRepository,
    actionsRepository,
    gatesRepository,
    navigation
) {

    override val header = SettingsActionsItem.Header(R.string.help_action, ::onHeaderDismissClicked, ::onHeaderClicked)
    override val switchChanged = null
    private val showHeaderSetting = settings.actionsDoubleTapShowHelp
    override val showHeader = showHeaderSetting.asFlow().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        settings.actionsDoubleTapShowHelp.getSync()
    )

    override suspend fun getActions() = actionsRepository.getSavedDoubleTapActions()
    override suspend fun getWhenGates() = whenGatesRepository.getWhenGates()

    override fun createAction(uiAction: TapTapUIAction, index: Int): DoubleTapAction {
        return DoubleTapAction(
            id = randomId(),
            name = uiAction.tapAction.name,
            index = index,
            extraData = uiAction.extraData
        )
    }

    override suspend fun addActionToRepo(action: DoubleTapAction): Long {
        return actionsRepository.addDoubleTapAction(action)
    }

    override fun removeAction(id: Int) {
        viewModelScope.launch {
            actionsRepository.deleteDoubleTapAction(id)
        }
    }

    override fun moveActionInternal(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            actionsRepository.moveDoubleTapAction(fromIndex, toIndex)
        }
    }

    override suspend fun getNextActionIndex(): Int {
        return actionsRepository.getNextDoubleTapActionIndex()
    }

    override fun onWhenGateChipClicked(action: TapTapUIAction) {
        viewModelScope.launch {
            navigation.navigate(SettingsActionsDoubleFragmentDirections.actionSettingsActionsDoubleFragmentToSettingsActionsWhenGatesFragment(action, false))
        }
    }

    private fun onHeaderDismissClicked() {
        viewModelScope.launch {
            showHeaderSetting.set(false)
        }
    }

    private fun onHeaderClicked(){
        viewModelScope.launch {
            navigation.navigate(SettingsActionsDoubleFragmentDirections.actionSettingsActionsDoubleFragmentToSettingsActionsHelpBottomSheetFragment())
        }
    }

}