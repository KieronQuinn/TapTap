package com.kieronquinn.app.taptap.ui.screens.settings.action

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.components.dragrecycler.RecyclerViewItemMoveCallback
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel.Companion.REQUEST_KEY_ADD_ACTION
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel.Companion.RESULT_KEY_ACTION
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel.Companion.REQUEST_KEY_ADD_GATE
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel.Companion.RESULT_KEY_GATE

abstract class SettingsActionViewModel(tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    val tripleTapEnabled = tapSharedPreferences.flowSharedPreferences.getBoolean(TapSharedPreferences.SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH, false).asFlow().asLiveData()

    val blockingWarning = MutableLiveData<BlockingWarning>(BlockingWarning())

    abstract val actions: LiveData<Array<ActionInternal>>

    open fun navigateToAddDialog(fragment: Fragment){
        fragment.setFragmentResultListener(REQUEST_KEY_ADD_ACTION){ requestKey, bundle ->
            if(requestKey == REQUEST_KEY_ADD_ACTION){
                val result = bundle.getParcelable<ActionInternal>(RESULT_KEY_ACTION) ?: return@setFragmentResultListener
                addAction(result)
            }
        }
    }

    abstract fun addAction(action: ActionInternal)

    abstract fun addGateForAction(whenGateInternal: WhenGateInternal, position: Int)

    abstract fun getActions()

    abstract fun onHeaderClicked(fragment: Fragment)

    abstract fun showWhenGateFragment(fragment: Fragment, currentGates: List<TapGate>)

    fun navigateToAddGateDialog(fragment: Fragment, position: Int, adapter: SettingsActionAdapter){
        fragment.setFragmentResultListener(REQUEST_KEY_ADD_GATE){ requestKey, bundle ->
            if(requestKey == REQUEST_KEY_ADD_GATE){
                val result = bundle.getParcelable<GateInternal>(RESULT_KEY_GATE) ?: return@setFragmentResultListener
                addGateForAction(result.toWhenGate(), position)
                adapter.notifyItemChanged(position + 1)
                updateInfoPosition()
            }
        }
        showWhenGateFragment(fragment, actions.value!![position].whenList.map { it.gate })
    }

    val state by lazy {
        MediatorLiveData<State>().apply {
            addSource(actions) {
                update(if (it.isEmpty()) State.Empty else State.Loaded(it.toMutableList()))
            }
        }
    }

    val selectedState = MutableLiveData<SelectedState>(SelectedState.None())
    val fabState = MediatorLiveData<Boolean>().apply {
        addSource(selectedState){
            update(it is SelectedState.ActionSelected)
        }
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    fun getItemTouchHelper(adapter: SettingsActionAdapter): ItemTouchHelper {
        return ItemTouchHelper(RecyclerViewItemMoveCallback(adapter)).apply {
            itemTouchHelper = this
        }
    }

    fun onRecyclerViewDragStart(viewHolder: RecyclerView.ViewHolder){
        itemTouchHelper.startDrag(viewHolder)
    }

    fun commitChanges() {
        (state.value as? State.Loaded)?.actions?.let { actions ->
            saveActions(actions)
        }
        updateInfoPosition()
    }

    fun updateInfoPosition() {
        (state.value as? State.Loaded)?.actions?.let { items ->
            val position = items.find { it.isBlocking() }?.let {
                items.indexOf(it) + 1
            } ?: run {
                -1
            }
            updateBlockingWarning(position)
        }
    }

    abstract fun saveActions(actions: MutableList<ActionInternal>)

    fun setSelectedIndex(adapterPosition: Int) {
        with(selectedState.value) {
            when (this) {
                is SelectedState.ActionSelected -> {
                    if (selectedItemIndex == adapterPosition || adapterPosition == -1) {
                        selectedState.update(SelectedState.None(selectedItemIndex))
                    }else{
                        selectedState.update(SelectedState.ActionSelected(adapterPosition, selectedItemIndex))
                    }
                }
                is SelectedState.None -> {
                    selectedState.update(SelectedState.ActionSelected(adapterPosition))
                }
            }
        }
    }

    fun getSelectedIndex(): Int {
        return with(selectedState.value) {
            when(this) {
                is SelectedState.ActionSelected -> this.selectedItemIndex
                else -> -1
            }
        }
    }

    fun swapSelected(from: Int, to: Int) {
        with(selectedState.value){
            if(this is SelectedState.ActionSelected && this.selectedItemIndex == from){
                selectedState.update(SelectedState.ActionSelected(to))
            }
            if(blockingWarning.value?.position == from){
                blockingWarning.update(BlockingWarning(to, blockingWarning.value?.previousPosition ?: -1))
            }
            if(blockingWarning.value?.position == to){
                blockingWarning.update(BlockingWarning(from, blockingWarning.value?.previousPosition ?: -1))
            }
        }
    }

    fun onFabClicked(fragment: Fragment, adapter: SettingsActionAdapter) = with(selectedState.value) {
        when(this){
            is SelectedState.None -> {
                navigateToAddDialog(fragment)
            }
            is SelectedState.ActionSelected -> {
                (state.value as? State.Loaded)?.actions?.removeAt(this.selectedItemIndex - 1)
                adapter.notifyItemRemoved(this.selectedItemIndex)
                commitChanges()
                selectedState.update(SelectedState.None())
            }
            else -> {}
        }
    }

    fun updateBlockingWarning(newPosition: Int){
        val currentPosition = blockingWarning.value?.position ?: -1
        blockingWarning.update(BlockingWarning(newPosition, currentPosition))
    }

    sealed class State {
        object Loading: State()
        data class Loaded(val actions: MutableList<ActionInternal>): State()
        object Empty: State()
    }

    sealed class SelectedState(open val previousSelectedIndex: Int = -1) {
        data class None(override val previousSelectedIndex: Int = -1): SelectedState(previousSelectedIndex)
        data class ActionSelected(val selectedItemIndex: Int, override val previousSelectedIndex: Int = -1): SelectedState(previousSelectedIndex)
    }

    data class BlockingWarning(val position: Int = -1, val previousPosition: Int = -1)

    override fun onBackPressed(fragment: Fragment): Boolean {
        return if(selectedState.value is SelectedState.ActionSelected){
            selectedState.update(SelectedState.None((selectedState.value as SelectedState.ActionSelected).selectedItemIndex))
            true
        }else {
            super.onBackPressed(fragment)
        }
    }

}