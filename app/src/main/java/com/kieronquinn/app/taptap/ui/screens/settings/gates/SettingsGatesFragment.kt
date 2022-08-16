package com.kieronquinn.app.taptap.ui.screens.settings.gates

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsGatesBinding
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModel.SettingsGatesItem.*
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModel.State
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddGenericFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.awaitState
import com.kieronquinn.app.taptap.utils.extensions.scrollToBottom
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SettingsGatesFragment :
    BoundFragment<FragmentSettingsGatesBinding>(FragmentSettingsGatesBinding::inflate),
    LockCollapsed, BackAvailable {

    private val viewModel by inject<SettingsGatesViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()

    private val adapter by lazy {
        SettingsGatesAdapter(
            binding.settingsGatesRecyclerview,
            ArrayList(),
            itemTouchHelper::startDrag,
            viewModel::onItemSelectionStateChange,
            viewModel::onItemStateChanged
        )
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
        setupResultListener()
        setupScrollToBottom()
        setupReloadService()
        viewModel.reloadGates()
    }

    private fun setupMonet() {
        binding.settingsGatesLoadingProgress.applyMonet()
    }

    private fun setupRecyclerView() = with(binding.settingsGatesRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsGatesFragment.adapter
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

    private fun handleState(state: State) {
        when (state) {
            is State.Loading -> {
                binding.settingsGatesLoading.isVisible = true
                binding.settingsGatesRecyclerview.isVisible = false
                binding.settingsGatesEmpty.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsGatesLoading.isVisible = false
                binding.settingsGatesRecyclerview.isVisible = true
                binding.settingsGatesEmpty.isVisible = state.items.filterNot { it is Header }.isEmpty()
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupFab() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.fabClicked.collect {
            when (it) {
                ContainerSharedViewModel.FabState.FabAction.ADD_GATE -> {
                    viewModel.onAddGateFabClicked()
                }
                ContainerSharedViewModel.FabState.FabAction.DELETE -> {
                    removeSelectedItem()
                    viewModel.onItemSelectionStateChange(false)
                }
                else -> {}
            }
        }
    }

    private fun removeSelectedItem() {
        val selectedItemIndex = adapter.removeSelectedItem() ?: return
        viewModel.removeGate(selectedItemIndex)
    }

    private fun setupFabState() {
        handleFabState(viewModel.fabState.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.fabState.collect {
                handleFabState(it)
            }
        }
    }

    private fun setupResultListener() {
        setFragmentResultListener(SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) { requestKey, bundle ->
            if (requestKey != SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) return@setFragmentResultListener
            val gate =
                bundle.getParcelable(SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) as? TapTapUIGate
                    ?: return@setFragmentResultListener
            adapter.addItem(Gate(gate))
            viewModel.onGateResult(gate)
        }
    }

    private fun setupScrollToBottom() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.scrollToBottomBus.collect {
            viewModel.state.awaitState(State.Loaded::class.java)
            binding.settingsGatesRecyclerview.scrollToBottom()
        }
    }

    private fun setupReloadService() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.reloadServiceBus.debounce(1000L).collect {
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

    private fun handleFabState(fabState: SettingsGatesViewModel.FabState) {
        val state = when (fabState) {
            SettingsGatesViewModel.FabState.HIDDEN -> ContainerSharedViewModel.FabState.Hidden
            SettingsGatesViewModel.FabState.ADD -> ContainerSharedViewModel.FabState.Shown(
                ContainerSharedViewModel.FabState.FabAction.ADD_GATE
            )
            SettingsGatesViewModel.FabState.DELETE -> ContainerSharedViewModel.FabState.Shown(
                ContainerSharedViewModel.FabState.FabAction.DELETE
            )
        }
        sharedViewModel.setFabState(state)
    }

    private fun hideFab() {
        sharedViewModel.setFabState(ContainerSharedViewModel.FabState.Hidden)
    }

    inner class ItemTouchHelperCallback : ItemTouchHelper.Callback() {

        private var startPos = -1

        override fun isLongPressDragEnabled() = false
        override fun isItemViewSwipeEnabled() = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPos = viewHolder.adapterPosition
            val endPos = target.adapterPosition
            val result = adapter.moveItem(startPos, endPos)
            this@SettingsGatesFragment.adapter.clearSelection()
            return result
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //No-op
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            if(target is SettingsGatesAdapter.ViewHolder.Header){
                return false
            }
            return super.canDropOver(recyclerView, current, target)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                startPos = viewHolder?.adapterPosition ?: -1
                (viewHolder as? SettingsGatesAdapter.ViewHolder)?.onRowSelectionChange(true)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if(startPos != -1){
                viewModel.moveGate(startPos, viewHolder.adapterPosition)
                startPos = -1
            }
            (viewHolder as? SettingsGatesAdapter.ViewHolder)?.onRowSelectionChange(false)
        }

    }

}