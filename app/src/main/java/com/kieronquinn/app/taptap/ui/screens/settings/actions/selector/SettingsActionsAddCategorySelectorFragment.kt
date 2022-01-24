package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsActionsAddCategorySelectorBinding
import com.kieronquinn.app.taptap.models.action.TapTapActionCategory
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.SettingsActionsAddCategorySelectorViewModel.State
import com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions.SettingsActionsActionSelectorAdapter
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActionsAddCategorySelectorFragment : SettingsActionsAddGenericFragment<FragmentSettingsActionsAddCategorySelectorBinding>(FragmentSettingsActionsAddCategorySelectorBinding::inflate), BackAvailable {

    override val viewModel by viewModel<SettingsActionsAddCategorySelectorViewModel>()

    private val categoryAdapter by lazy {
        SettingsActionsAddCategorySelectorAdapter(binding.settingsActionsAddCategorySelectorRecyclerview, emptyList(), ::onCategoryClicked)
    }

    private val actionsAdapter by lazy {
        SettingsActionsActionSelectorAdapter(binding.settingsActionsAddCategorySelectorRecyclerview, emptyList(), viewModel::getActionSupportedRequirement, ::showSnackbarForChip, ::onActionClicked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
        setupSearch()
        setupSearchClear()
    }

    private fun setupMonet() {
        binding.settingsActionsAddCategorySelectorLoadingProgress.applyMonet()
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupRecyclerView() = with(binding.settingsActionsAddCategorySelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        applyBottomInsets(binding.root, -resources.getDimension(R.dimen.search_box_negative_margin).toInt())
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
        when(state) {
            is State.Loading -> {
                binding.settingsActionsAddCategorySelectorEmpty.isVisible = false
                binding.settingsActionsAddCategorySelectorLoading.isVisible = true
                binding.settingsActionsAddCategorySelectorRecyclerview.isVisible = false
            }
            is State.CategoryPicker -> {
                binding.settingsActionsAddCategorySelectorEmpty.isVisible = state.categories.isEmpty()
                binding.settingsActionsAddCategorySelectorLoading.isVisible = false
                binding.settingsActionsAddCategorySelectorRecyclerview.isVisible = true
                binding.settingsActionsAddCategorySelectorRecyclerview.setAdapterIfRequired(categoryAdapter)
                categoryAdapter.items = state.categories
                categoryAdapter.notifyDataSetChanged()
            }
            is State.ItemPicker -> {
                binding.settingsActionsAddCategorySelectorEmpty.isVisible = state.items.isEmpty()
                binding.settingsActionsAddCategorySelectorLoading.isVisible = false
                binding.settingsActionsAddCategorySelectorRecyclerview.isVisible = true
                binding.settingsActionsAddCategorySelectorRecyclerview.setAdapterIfRequired(actionsAdapter)
                actionsAdapter.items = state.items
                actionsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupSearch() {
        setSearchText(viewModel.searchText.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                binding.includeSearch.searchBox.onChanged().debounce(250L).collect {
                    viewModel.setSearchText(it ?: "")
                }
            }
        }
    }

    private fun setupSearchClear() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
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

    private fun RecyclerView.setAdapterIfRequired(adapter: RecyclerView.Adapter<*>) {
        if(this.adapter != adapter){
            setAdapter(adapter)
        }
    }

    private fun onCategoryClicked(category: TapTapActionCategory){
        viewModel.onCategoryClicked(category)
    }

    override fun onDestroyView() {
        binding.settingsActionsAddCategorySelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

}