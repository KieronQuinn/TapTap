package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedPackageSelectorBinding
import com.kieronquinn.app.taptap.models.shared.ARG_NAME_SHARED_ARGUMENT
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.ProvidesTitle
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onChanged
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedPackageSelectorFragment :
    BoundFragment<FragmentSettingsSharedPackageSelectorBinding>(
        FragmentSettingsSharedPackageSelectorBinding::inflate
    ), BackAvailable, ProvidesTitle {

    companion object {
        const val FRAGMENT_RESULT_KEY_PACKAGE = "fragment_result_package"
        const val FRAGMENT_EXTRA_TITLE = "title"
        const val FRAGMENT_EXTRA_SHOW_ALL_APPS = "show_all_apps"
    }

    private val viewModel by viewModel<SettingsSharedPackageSelectorViewModel>()
    private val args by navArgs<SettingsSharedPackageSelectorFragmentArgs>()

    private val adapter by lazy {
        SettingsSharedPackageSelectorAdapter(
            binding.settingsSharedPackageSelectorRecyclerview,
            emptyList(),
            ::onAppClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
        setupSearch()
        setupSearchClear()
        viewModel.setShowAllApps(args.showAllApps)
    }

    private fun setupMonet() {
        binding.settingsSharedPackageSelectorLoadingProgress.applyMonet()
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupRecyclerView() = with(binding.settingsSharedPackageSelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsSharedPackageSelectorFragment.adapter
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
        when (state) {
            is State.Loading -> {
                binding.settingsSharedPackageSelectorLoading.isVisible = true
                binding.settingsSharedPackageSelectorEmpty.isVisible = false
                binding.settingsSharedPackageSelectorRecyclerview.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsSharedPackageSelectorLoading.isVisible = false
                binding.settingsSharedPackageSelectorEmpty.isVisible = state.items.isEmpty()
                binding.settingsSharedPackageSelectorRecyclerview.isVisible = true
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupSearch() {
        setSearchText(viewModel.searchText.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.includeSearch.searchBox.onChanged().debounce(250L).collect {
                viewModel.setSearchText(it ?: "")
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

    private fun onAppClicked(packageName: String) {
        setFragmentResult(FRAGMENT_RESULT_KEY_PACKAGE, formResultBundle(packageName))
        viewModel.onAppClicked()
    }

    private fun formResultBundle(packageName: String): Bundle {
        return bundleOf(
            FRAGMENT_RESULT_KEY_PACKAGE to packageName,
            ARG_NAME_SHARED_ARGUMENT to args.argument
        )
    }

    override fun getTitle(): CharSequence? {
        return getString(args.title)
    }

}