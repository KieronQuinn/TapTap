package com.kieronquinn.app.taptap.ui.screens.setup.configuration

import android.content.Context
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.core.columbus.actions.ChannelAction
import com.kieronquinn.app.taptap.core.TapColumbusService
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseFragment
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SetupConfigurationViewModel(private val tapColumbusService: TapColumbusService): BaseViewModel() {

    val tapEvent = MutableLiveData<ChannelAction.TapEvent>()
    val infoboxTransitioned = MediatorLiveData<Boolean>().apply {
        addSource(tapEvent){
            update(it != null)
        }
    }

    fun setup(context: Context) = viewModelScope.launch {
        setupTapActions(context).collect {
            tapEvent.postValue(it)
        }
    }

    @ExperimentalCoroutinesApi
    private fun setupTapActions(context: Context): Flow<ChannelAction.TapEvent> {
        tapColumbusService.setDemoMode(true)
        val doubleClickFlow = ChannelAction(context, ChannelAction.TapEvent.DOUBLE).apply {
            Log.d(TapColumbusService.TAG, "setting action to channelaction")
            tapColumbusService.actions = listOf(this)
            tapColumbusService.updateSensorListener()
        }.run {
            runFlow.receiveAsFlow()
        }
        val tripleClickFlow = ChannelAction(context, ChannelAction.TapEvent.TRIPLE).apply {
            Log.d(TapColumbusService.TAG, "setting triple action to channelaction")
            tapColumbusService.tripleTapActions = listOf(this).toMutableList()
            tapColumbusService.updateActiveActionTriple()
        }.run {
            runFlow.receiveAsFlow()
        }
        return merge(doubleClickFlow, tripleClickFlow)
    }

    fun reset(){
        tapColumbusService.setDemoMode(false)
    }

    fun onTroubleshootingClicked(fragment: BaseFragment){
        fragment.navigate(SetupConfigurationFragmentDirections.actionSetupConfigurationFragmentToTroubleshootingBottomSheetDialogFragment())
    }

    fun onNextClicked(fragment: BaseFragment){
        fragment.navigate(SetupConfigurationFragmentDirections.actionSetupConfigurationFragmentToSetupFossInfoFragment())
    }

}