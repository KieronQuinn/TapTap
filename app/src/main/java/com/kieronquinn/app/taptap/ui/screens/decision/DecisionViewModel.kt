package com.kieronquinn.app.taptap.ui.screens.decision

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.backuprestore.RestoreRepository
import com.kieronquinn.app.taptap.utils.extensions.deviceHasGyroscope
import com.kieronquinn.app.taptap.utils.extensions.isColumbusEnabled
import com.kieronquinn.app.taptap.utils.extensions.isNativeColumbusEnabled
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class DecisionViewModel: ViewModel() {

    abstract val decisionMade: Flow<Unit>

}

class DecisionViewModelImpl(context: Context, private val rootNavigation: RootNavigation, private val settings: TapTapSettings, private val restoreRepository: RestoreRepository): DecisionViewModel() {

    private val _decisionMade = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
        viewModelScope.launch {
            rootNavigation.navigate(getDestination(context))
            emit(Unit)
        }
    }

    override val decisionMade = _decisionMade.asSharedFlow()

    private suspend fun getDestination(context: Context): NavDirections {
        return when {
            !context.deviceHasGyroscope() -> {
                DecisionFragmentDirections.actionDecisionFragmentToNoGyroscopeFragment()
            }
            context.isColumbusEnabled() && !context.isNativeColumbusEnabled() -> {
                DecisionFragmentDirections.actionDecisionFragmentToDisableColumbusFragment()
            }
            restoreRepository.shouldUpgrade(context) -> {
                DecisionFragmentDirections.actionDecisionFragmentToSetupUpgradeFragment()
            }
            settings.hasSeenSetup.get() -> {
                DecisionFragmentDirections.actionDecisionFragmentToNavGraphSettings()
            }
            else -> {
                DecisionFragmentDirections.actionDecisionFragmentToNavGraphSetup()
            }
        }
    }

}