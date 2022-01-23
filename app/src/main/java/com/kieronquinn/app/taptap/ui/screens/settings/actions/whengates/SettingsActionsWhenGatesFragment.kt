package com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsActionsWhenGatesBinding
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.models.gate.TapTapUIWhenGate
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModel.State
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddGenericFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.scrollToBottom
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActionsWhenGatesFragment :
    BoundFragment<FragmentSettingsActionsWhenGatesBinding>(FragmentSettingsActionsWhenGatesBinding::inflate),
    LockCollapsed, BackAvailable {

    companion object {
        const val FRAGMENT_RESULT_KEY_WHEN_GATES_SIZE = "fragment_result_when_gates_size"
    }

    private val viewModelDouble by viewModel<SettingsActionsWhenGatesViewModelDouble>()
    private val viewModelTriple by viewModel<SettingsActionsWhenGatesViewModelTriple>()

    private val viewModel: SettingsActionsWhenGatesViewModel by lazy {
        if(args.isTriple){
            viewModelTriple
        }else{
            viewModelDouble
        }
    }

    private val args by navArgs<SettingsActionsWhenGatesFragmentArgs>()
    private val action: TapTapUIAction by lazy {
        args.action
    }

    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val adapter by lazy {
        SettingsActionsWhenGatesAdapter(
            binding.settingsActionsWhenGatesRecyclerview,
            monet.getPrimaryColor(requireContext()),
            viewModel::onItemSelectionStateChange,
            ArrayList()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupAction()
        setupRecyclerView()
        setupState()
        setupFabState()
        setupFab()
        setupResultListener()
        setupScrollToBottom()
        setupReloadService()
        viewModel.setupWithAction(action, requireContext())
    }

    private fun setupAction() = with(binding.settingsActionsWhenGatesAction) {
        root.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(requireContext()))
        root.foreground = null
        itemSettingsActionsActionChip.isVisible = false
        itemActionChipWhen.isVisible = false
        itemSettingsActionsActionHandle.isVisible = false
        itemSettingsActionsActionTitle.text = getString(action.tapAction.nameRes)
        itemSettingsActionsActionContent.text = action.description
        itemSettingsActionsActionIcon.setImageResource(action.tapAction.iconRes)
    }

    private fun setupMonet() {
        binding.settingsActionsWhenGatesAppbar.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
        binding.settingsActionsWhenGatesLoadingProgress.applyMonet()
    }

    private fun setupRecyclerView() = with(binding.settingsActionsWhenGatesRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsActionsWhenGatesFragment.adapter
        applyBottomInsets(binding.root, resources.getDimension(R.dimen.container_fab_margin).toInt())
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
                binding.settingsActionsWhenGatesLoading.isVisible = true
                binding.settingsActionsWhenGatesRecyclerview.isVisible = false
                binding.settingsActionsWhenGatesEmpty.isVisible = false
            }
            is State.Loaded -> {
                setFragmentResult(FRAGMENT_RESULT_KEY_WHEN_GATES_SIZE, bundleOf(FRAGMENT_RESULT_KEY_WHEN_GATES_SIZE to Pair(state.action.id, state.gates.size)))
                binding.settingsActionsWhenGatesLoading.isVisible = false
                binding.settingsActionsWhenGatesRecyclerview.isVisible = state.gates.isNotEmpty()
                binding.settingsActionsWhenGatesEmpty.isVisible = state.gates.isEmpty()
                adapter.items = state.gates.map {
                    SettingsActionsWhenGatesViewModel.SettingsWhenGatesItem(it)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupFabState() {
        handleFabState(viewModel.fabState.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.fabState.collect {
                handleFabState(it)
            }
        }
    }

    private fun setupFab() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.fabClicked.collect {
            when (it) {
                ContainerSharedViewModel.FabState.FabAction.ADD_REQUIREMENT -> {
                    viewModel.onAddRequirementFabClicked()
                }
                ContainerSharedViewModel.FabState.FabAction.DELETE -> {
                    removeSelectedItem()
                    viewModel.onItemSelectionStateChange(false)
                }
            }
        }
    }

    private fun handleFabState(fabState: SettingsActionsWhenGatesViewModel.FabState) {
        val state = when (fabState) {
            SettingsActionsWhenGatesViewModel.FabState.HIDDEN -> ContainerSharedViewModel.FabState.Hidden
            SettingsActionsWhenGatesViewModel.FabState.ADD -> ContainerSharedViewModel.FabState.Shown(
                ContainerSharedViewModel.FabState.FabAction.ADD_REQUIREMENT
            )
            SettingsActionsWhenGatesViewModel.FabState.DELETE -> ContainerSharedViewModel.FabState.Shown(
                ContainerSharedViewModel.FabState.FabAction.DELETE
            )
        }
        sharedViewModel.setFabState(state)
    }

    private fun removeSelectedItem() {
        val selectedItemId = adapter.getSelectedItemId() ?: return
        viewModel.removeWhenGate(action.id, selectedItemId)
    }

    private fun setupResultListener() {
        setFragmentResultListener(SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) { requestKey, bundle ->
            if (requestKey != SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) return@setFragmentResultListener
            val gate = bundle.getParcelable(SettingsGatesAddGenericFragment.FRAGMENT_RESULT_KEY_GATE) as? TapTapUIGate ?: return@setFragmentResultListener
            //ID is sorted later
            val whenGate = TapTapUIWhenGate(-1, gate, false)
            viewModel.handleGateResult(action.id, whenGate, requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
        hideFab()
    }

    private fun hideFab() {
        sharedViewModel.setFabState(ContainerSharedViewModel.FabState.Hidden)
    }

    private fun setupReloadService() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.reloadServiceBus.debounce(1000L).collect {
            sharedViewModel.restartService(requireContext())
        }
    }

    private fun setupScrollToBottom() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.scrollToBottomBus.collect {
            binding.settingsActionsWhenGatesRecyclerview.scrollToBottom()
        }
    }

}