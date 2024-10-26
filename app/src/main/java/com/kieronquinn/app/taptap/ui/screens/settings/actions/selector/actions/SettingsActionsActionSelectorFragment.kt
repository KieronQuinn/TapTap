package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsActionsAddActionSelectorBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.ProvidesTitle
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddGenericFragment
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions.SettingsActionsActionSelectorViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActionsActionSelectorFragment :
    SettingsActionsAddGenericFragment<FragmentSettingsActionsAddActionSelectorBinding>(FragmentSettingsActionsAddActionSelectorBinding::inflate), ProvidesTitle, BackAvailable {

    override val viewModel by viewModel<SettingsActionsActionSelectorViewModel>()
    private val args by navArgs<SettingsActionsActionSelectorFragmentArgs>()

    private val adapter by lazy {
        SettingsActionsActionSelectorAdapter(binding.settingsActionsAddActionSelectorRecyclerview, emptyList(), viewModel::getActionSupportedRequirement, ::showSnackbarForChip, ::onActionClicked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
        setupSearch()
        setupSearchClear()
        viewModel.setupWithCategory(args.category, requireContext())
    }

    private fun setupMonet() {
        binding.settingsActionsAddActionSelectorLoadingProgress.applyMonet()
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupRecyclerView() = with(binding.settingsActionsAddActionSelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsActionsActionSelectorFragment.adapter
        applyBottomInsets(binding.root, -resources.getDimension(R.dimen.search_box_negative_margin).toInt())
    }

    private fun setupState(){
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.settingsActionsAddActionSelectorEmpty.isVisible = false
                binding.settingsActionsAddActionSelectorLoading.isVisible = true
                binding.settingsActionsAddActionSelectorRecyclerview.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsActionsAddActionSelectorEmpty.isVisible = state.actions.isEmpty()
                binding.settingsActionsAddActionSelectorLoading.isVisible = false
                binding.settingsActionsAddActionSelectorRecyclerview.isVisible = true
                adapter.items = state.actions
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupSearch() {
        setSearchText(viewModel.searchText.value)
        whenResumed {
            launch {
                binding.includeSearch.searchBox.onChanged().debounce(250L).collect {
                    viewModel.setSearchText(it ?: "")
                }
            }
        }
    }

    private fun setupSearchClear() = whenResumed {
        launch {
            viewModel.searchShowClear.collect {
                binding.includeSearch.searchClear.isVisible = it
            }
        }
        launch {
            binding.includeSearch.searchClear.onClicked().collect {
                setSearchText("")
            }
        }
    }

    private fun setSearchText(text: CharSequence) {
        binding.includeSearch.searchBox.run {
            this.text?.let {
                it.clear()
                it.append(text)
            } ?: setText(text)
        }
    }

    override fun getTitle(): CharSequence? {
        return context?.getString(args.category.labelRes)
    }

    override fun onDestroyView() {
        binding.settingsActionsAddActionSelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

}