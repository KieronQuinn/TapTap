package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsGatesAddGateSelectorBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.ProvidesTitle
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.SettingsGatesAddGenericFragment
import com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates.SettingsGatesGateSelectorViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsGatesGateSelectorFragment :
    SettingsGatesAddGenericFragment<FragmentSettingsGatesAddGateSelectorBinding>(FragmentSettingsGatesAddGateSelectorBinding::inflate), ProvidesTitle, BackAvailable {

    override val viewModel by viewModel<SettingsGatesGateSelectorViewModel>()
    private val args by navArgs<SettingsGatesGateSelectorFragmentArgs>()

    private val adapter by lazy {
        SettingsGatesGateSelectorAdapter(binding.settingsGatesAddGateSelectorRecyclerview, emptyList(), viewModel::isGateSupported, ::showSnackbarForChip, ::onGateClicked, args.isRequirement)
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
        binding.settingsGatesAddGateSelectorLoadingProgress.applyMonet()
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupRecyclerView() = with(binding.settingsGatesAddGateSelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsGatesGateSelectorFragment.adapter
        applyBottomInsets(binding.root, -resources.getDimension(R.dimen.search_box_negative_margin).toInt())
    }

    private fun setupState(){
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
                binding.settingsGatesAddGateSelectorEmpty.isVisible = false
                binding.settingsGatesAddGateSelectorLoading.isVisible = true
                binding.settingsGatesAddGateSelectorRecyclerview.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsGatesAddGateSelectorEmpty.isVisible = state.gates.isEmpty()
                binding.settingsGatesAddGateSelectorLoading.isVisible = false
                binding.settingsGatesAddGateSelectorRecyclerview.isVisible = true
                adapter.items = state.gates
                adapter.notifyDataSetChanged()
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

    override fun getTitle(): CharSequence? {
        return context?.getString(args.category.labelRes)
    }

    override fun onDestroyView() {
        binding.settingsGatesAddGateSelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

}