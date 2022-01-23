package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsGatesAddCategorySelectorBinding
import com.kieronquinn.app.taptap.models.gate.TapTapGateCategory
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.ProvidesTitle
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates.SettingsGatesGateSelectorAdapter
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsGatesAddCategorySelectorFragment : SettingsGatesAddGenericFragment<FragmentSettingsGatesAddCategorySelectorBinding>(
    FragmentSettingsGatesAddCategorySelectorBinding::inflate), BackAvailable, ProvidesTitle {

    override val viewModel by viewModel<SettingsGatesAddCategorySelectorViewModel>()

    private val args by navArgs<SettingsGatesAddCategorySelectorFragmentArgs>()

    private val categoryAdapter by lazy {
        SettingsGatesAddCategorySelectorAdapter(binding.settingsGatesAddCategorySelectorRecyclerview, emptyList(), ::onCategoryClicked)
    }

    private val gatesAdapter by lazy {
        SettingsGatesGateSelectorAdapter(binding.settingsGatesAddCategorySelectorRecyclerview, emptyList(), viewModel::isGateSupported, ::showSnackbarForChip, ::onGateClicked, args.isRequirement)
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
        binding.settingsGatesAddCategorySelectorLoadingProgress.applyMonet()
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupRecyclerView() = with(binding.settingsGatesAddCategorySelectorRecyclerview) {
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

    private fun handleState(state: SettingsGatesAddCategorySelectorViewModel.State) {
        when(state) {
            is SettingsGatesAddCategorySelectorViewModel.State.Loading -> {
                binding.settingsGatesAddCategorySelectorEmpty.isVisible = false
                binding.settingsGatesAddCategorySelectorLoading.isVisible = true
                binding.settingsGatesAddCategorySelectorRecyclerview.isVisible = false
            }
            is SettingsGatesAddCategorySelectorViewModel.State.CategoryPicker -> {
                binding.settingsGatesAddCategorySelectorEmpty.isVisible = state.categories.isEmpty()
                binding.settingsGatesAddCategorySelectorLoading.isVisible = false
                binding.settingsGatesAddCategorySelectorRecyclerview.isVisible = true
                binding.settingsGatesAddCategorySelectorRecyclerview.setAdapterIfRequired(categoryAdapter)
                categoryAdapter.items = state.categories
                categoryAdapter.notifyDataSetChanged()
            }
            is SettingsGatesAddCategorySelectorViewModel.State.ItemPicker -> {
                binding.settingsGatesAddCategorySelectorEmpty.isVisible = state.items.isEmpty()
                binding.settingsGatesAddCategorySelectorLoading.isVisible = false
                binding.settingsGatesAddCategorySelectorRecyclerview.isVisible = true
                binding.settingsGatesAddCategorySelectorRecyclerview.setAdapterIfRequired(gatesAdapter)
                gatesAdapter.items = state.items
                gatesAdapter.notifyDataSetChanged()
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

    private fun onCategoryClicked(category: TapTapGateCategory){
        viewModel.onCategoryClicked(category, args.isRequirement)
    }

    override fun onDestroyView() {
        binding.settingsGatesAddCategorySelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

    override fun getTitle(): CharSequence {
        return if(args.isRequirement){
            getString(R.string.fab_add_requirement)
        }else{
            getString(R.string.fab_add_gate)
        }
    }

}