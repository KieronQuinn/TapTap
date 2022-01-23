package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedQuickSettingsSelectorBinding
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting.SettingsSharedQuickSettingSelectorViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedQuickSettingSelectorFragment: BoundFragment<FragmentSettingsSharedQuickSettingsSelectorBinding>(FragmentSettingsSharedQuickSettingsSelectorBinding::inflate), BackAvailable {

    companion object {
        const val FRAGMENT_RESULT_KEY_QUICK_SETTING = "fragment_result_quick_setting"
    }

    private val viewModel by viewModel<SettingsSharedQuickSettingSelectorViewModel>()
    private val adapter by lazy {
        SettingsSharedQuickSettingSelectorAdapter(binding.settingsSharedQuickSettingsRecyclerview, emptyList(), ::onQuickSettingClicked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
    }

    private fun setupMonet() {
        binding.settingsSharedQuickSettingsLoadingProgress.applyMonet()
    }

    private fun setupRecyclerView() = with(binding.settingsSharedQuickSettingsRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsSharedQuickSettingSelectorFragment.adapter
        applyBottomInsets(binding.root)
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
        when(state){
            is State.Loading -> {
                binding.settingsSharedQuickSettingsLoading.isVisible = true
                binding.settingsSharedQuickSettingsEmpty.isVisible = false
                binding.settingsSharedQuickSettingsRecyclerview.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsSharedQuickSettingsLoading.isVisible = false
                binding.settingsSharedQuickSettingsEmpty.isVisible = state.tiles.isEmpty()
                binding.settingsSharedQuickSettingsRecyclerview.isVisible = state.tiles.isNotEmpty()
                adapter.items = state.tiles
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun onQuickSettingClicked(quickSetting: QuickSettingsRepository.QuickSetting){
        setFragmentResult(FRAGMENT_RESULT_KEY_QUICK_SETTING, bundleOf(FRAGMENT_RESULT_KEY_QUICK_SETTING to quickSetting))
        viewModel.onQuickSettingClicked()
    }

}