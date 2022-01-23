package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.actions.custom.LaunchAppShortcutAction
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedAppShortcutsSelectorBinding
import com.kieronquinn.app.taptap.models.columbus.AppShortcutData
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts.SettingsSharedAppShortcutsSelectorViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedAppShortcutsSelectorFragment :
    BoundFragment<FragmentSettingsSharedAppShortcutsSelectorBinding>(FragmentSettingsSharedAppShortcutsSelectorBinding::inflate), BackAvailable {

    companion object {
        const val FRAGMENT_RESULT_KEY_APP_SHORTCUT = "fragment_result_app_shortcut"
    }

    private val viewModel by viewModel<SettingsSharedAppShortcutsSelectorViewModel>()

    private val adapter by lazy {
        SettingsSharedAppShortcutsSelectorAdapter(binding.settingsSharedAppShortcutsSelectorRecyclerview, emptyList(), viewModel::onAppClicked, ::onAppShortcutClicked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupState()
        setupRecyclerView()
    }

    private fun setupMonet() {
        binding.settingsSharedAppShortcutsSelectorLoadingProgress.applyMonet()
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
                binding.settingsSharedAppShortcutsSelectorRecyclerview.isVisible = false
                binding.settingsSharedAppShortcutsSelectorError.isVisible = false
                binding.settingsSharedAppShortcutsSelectorLoading.isVisible = true
            }
            is State.Loaded -> {
                binding.settingsSharedAppShortcutsSelectorLoading.isVisible = false
                binding.settingsSharedAppShortcutsSelectorError.isVisible = false
                binding.settingsSharedAppShortcutsSelectorRecyclerview.isVisible = true
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
            is State.Error -> {
                binding.settingsSharedAppShortcutsSelectorLoading.isVisible = false
                binding.settingsSharedAppShortcutsSelectorRecyclerview.isVisible = false
                binding.settingsSharedAppShortcutsSelectorError.isVisible = true
                binding.settingsSharedAppShortcutsSelectorErrorLabel.text = getString(state.reason.contentRes)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.settingsSharedAppShortcutsSelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsSharedAppShortcutsSelectorFragment.adapter
        applyBottomInsets(binding.root)
    }

    private fun onAppShortcutClicked(shortcut: AppShortcutData) {
        setFragmentResult(FRAGMENT_RESULT_KEY_APP_SHORTCUT, bundleOf(FRAGMENT_RESULT_KEY_APP_SHORTCUT to shortcut))
        viewModel.onAppShortcutClicked()
    }

    override fun onDestroyView() {
        binding.settingsSharedAppShortcutsSelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

}