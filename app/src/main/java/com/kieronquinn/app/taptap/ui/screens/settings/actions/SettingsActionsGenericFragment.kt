package com.kieronquinn.app.taptap.ui.screens.settings.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.repositories.room.actions.Action
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericViewModel.SettingsActionsItem.*
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddGenericFragment.Companion.FRAGMENT_RESULT_KEY_ACTION
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.awaitState
import com.kieronquinn.app.taptap.utils.extensions.scrollToBottom
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class SettingsActionsGenericFragment<T: ViewBinding, A: Action>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): BoundFragment<T>(inflate), LockCollapsed, BackAvailable {

    abstract fun getRecyclerView(): RecyclerView
    abstract fun getLoadingProgressView(): LinearProgressIndicator
    abstract fun getLoadingView(): Group
    abstract fun getEmptyView(): Group

    abstract val viewModel: SettingsActionsGenericViewModel<A>

    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()

    private val adapter by lazy {
        SettingsActionsGenericAdapter(getRecyclerView(), ArrayList(), itemTouchHelper::startDrag, viewModel::onItemSelectionStateChange, viewModel::onWhenGateChipClicked)
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchHelperCallback())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
        setupFab()
        setupFabState()
        setupResultListeners()
        setupScrollToBottom()
        setupReloadService()
        setupSwitchReloadService()
        viewModel.reloadActions()
    }

    private fun setupMonet() {
        getLoadingProgressView().applyMonet()
    }

    private fun setupRecyclerView() = with(getRecyclerView()) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsActionsGenericFragment.adapter
        applyBottomInsets(binding.root, resources.getDimension(R.dimen.container_fab_margin).toInt())
        itemTouchHelper.attachToRecyclerView(this)
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: SettingsActionsGenericViewModel.State) {
        when(state) {
            is SettingsActionsGenericViewModel.State.Loading -> {
                getLoadingView().isVisible = true
                getRecyclerView().isVisible = false
                getEmptyView().isVisible = false
            }
            is SettingsActionsGenericViewModel.State.Loaded -> {
                getLoadingView().isVisible = false
                getRecyclerView().isVisible = true
                getEmptyView().isVisible = state.items.filterNot { it is Header }.isEmpty()
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupFab() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.fabClicked.collect {
            when (it) {
                ContainerSharedViewModel.FabState.FabAction.ADD_ACTION -> {
                    viewModel.onAddActionFabClicked()
                }
                ContainerSharedViewModel.FabState.FabAction.DELETE -> {
                    removeSelectedItem()
                    viewModel.onItemSelectionStateChange(false)
                }
            }
        }
    }

    private fun removeSelectedItem(){
        val selectedItemId = adapter.removeSelectedItem() ?: return
        viewModel.removeAction(selectedItemId)
    }

    private fun setupFabState() {
        handleFabState(viewModel.fabState.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.fabState.collect {
                handleFabState(it)
            }
        }
    }

    private fun setupResultListeners() {
        setFragmentResultListener(FRAGMENT_RESULT_KEY_ACTION) { requestKey, bundle ->
            if(requestKey != FRAGMENT_RESULT_KEY_ACTION) return@setFragmentResultListener
            val action = bundle.getParcelable(FRAGMENT_RESULT_KEY_ACTION) as? TapTapUIAction ?: return@setFragmentResultListener
            adapter.addItem(Action(action))
            viewModel.onActionResult(action)
        }
        setFragmentResultListener(SettingsActionsWhenGatesFragment.FRAGMENT_RESULT_KEY_WHEN_GATES_SIZE) { key, bundle ->
            val actionPair = bundle.getSerializable(SettingsActionsWhenGatesFragment.FRAGMENT_RESULT_KEY_WHEN_GATES_SIZE) as? Pair<Int, Int> ?: return@setFragmentResultListener
            adapter.updateWhenGatesSize(actionPair.first, actionPair.second)
        }
    }

    private fun setupScrollToBottom() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.scrollToBottomBus.collect {
            viewModel.state.awaitState(SettingsActionsGenericViewModel.State.Loaded::class.java)
            getRecyclerView().scrollToBottom()
        }
    }

    private fun setupReloadService() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.reloadServiceBus.debounce(1000L).collect {
            sharedViewModel.restartService(requireContext())
        }
    }

    private fun setupSwitchReloadService() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.switchChanged?.debounce(1000L)?.collect {
            sharedViewModel.restartService(requireContext())
        }
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
        hideFab()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun handleFabState(fabState: SettingsActionsGenericViewModel.FabState) {
        val state = when(fabState){
            SettingsActionsGenericViewModel.FabState.HIDDEN -> ContainerSharedViewModel.FabState.Hidden
            SettingsActionsGenericViewModel.FabState.ADD -> ContainerSharedViewModel.FabState.Shown(ContainerSharedViewModel.FabState.FabAction.ADD_ACTION)
            SettingsActionsGenericViewModel.FabState.DELETE -> ContainerSharedViewModel.FabState.Shown(ContainerSharedViewModel.FabState.FabAction.DELETE)
        }
        sharedViewModel.setFabState(state)
    }

    private fun hideFab() {
        sharedViewModel.setFabState(ContainerSharedViewModel.FabState.Hidden)
    }

    inner class ItemTouchHelperCallback: ItemTouchHelper.Callback() {

        private var startPos = -1

        override fun isLongPressDragEnabled() = false
        override fun isItemViewSwipeEnabled() = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return when(viewHolder){
                is SettingsActionsGenericAdapter.ViewHolder.Header -> 0
                is SettingsActionsGenericAdapter.ViewHolder.Action -> makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
                else -> 0
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPos = viewHolder.adapterPosition
            val endPos = target.adapterPosition
            val result = adapter.moveItem(startPos, endPos)
            this@SettingsActionsGenericFragment.adapter.clearSelection()
            return result
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            if(target is SettingsActionsGenericAdapter.ViewHolder.Header){
                return false
            }
            return super.canDropOver(recyclerView, current, target)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //No-op
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if(actionState != ItemTouchHelper.ACTION_STATE_IDLE){
                startPos = viewHolder?.adapterPosition ?: -1
                (viewHolder as? SettingsActionsGenericAdapter.ViewHolder)?.onRowSelectionChange(true)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if(startPos != -1){
                viewModel.moveAction(startPos, viewHolder.adapterPosition)
                startPos = -1
            }
            (viewHolder as? SettingsActionsGenericAdapter.ViewHolder)?.onRowSelectionChange(false)
        }

    }

}