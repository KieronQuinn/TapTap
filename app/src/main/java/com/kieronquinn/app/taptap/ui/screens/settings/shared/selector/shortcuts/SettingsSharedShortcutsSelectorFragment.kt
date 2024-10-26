package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsSharedShortcutsSelectorBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.serialize
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSharedShortcutsSelectorFragment :
    BoundFragment<FragmentSettingsSharedShortcutsSelectorBinding>(FragmentSettingsSharedShortcutsSelectorBinding::inflate), BackAvailable {

    companion object {
        const val FRAGMENT_RESULT_KEY_SHORTCUT = "fragment_result_shortcut"
    }

    private val adapter by lazy {
        SettingsSharedShortcutsSelectorAdapter(binding.settingsSharedShortcutsSelectorRecyclerview, emptyList(), viewModel::onAppClicked, ::onShortcutClicked)
    }

    private val viewModel by viewModel<SettingsSharedShortcutsSelectorViewModel>()

    private val activityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        it?.data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)?.let { intent ->
            onShortcutResult(intent)
        } ?: run {
            onUnsupportedShortcut()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupState()
        setupRecyclerView()
    }

    private fun setupMonet() {
        binding.settingsSharedShortcutsSelectorLoadingProgress.applyMonet()
    }

    private fun setupState(){
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: SettingsSharedShortcutsSelectorViewModel.State) {
        when(state){
            is SettingsSharedShortcutsSelectorViewModel.State.Loading -> {
                binding.settingsSharedShortcutsSelectorRecyclerview.isVisible = false
                binding.settingsSharedShortcutsSelectorError.isVisible = false
                binding.settingsSharedShortcutsSelectorLoading.isVisible = true
            }
            is SettingsSharedShortcutsSelectorViewModel.State.Loaded -> {
                binding.settingsSharedShortcutsSelectorLoading.isVisible = false
                binding.settingsSharedShortcutsSelectorError.isVisible = false
                binding.settingsSharedShortcutsSelectorRecyclerview.isVisible = true
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupRecyclerView() = with(binding.settingsSharedShortcutsSelectorRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsSharedShortcutsSelectorFragment.adapter
        applyBottomInsets(binding.root)
    }

    private fun onShortcutClicked(componentName: ComponentName) {
        Intent().apply {
            `package` = componentName.packageName
            component = componentName
        }.also {
            activityResultContract.launch(it)
        }
    }

    private fun onShortcutResult(intent: Intent) {
        val serializedIntent = intent.serialize() ?: run {
            onUnsupportedShortcut()
            return
        }
        setFragmentResult(FRAGMENT_RESULT_KEY_SHORTCUT, bundleOf(FRAGMENT_RESULT_KEY_SHORTCUT to serializedIntent))
        viewModel.unwind()
    }

    private fun onUnsupportedShortcut(){
        Toast.makeText(requireContext(), R.string.action_launch_shortcut_toast, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        binding.settingsSharedShortcutsSelectorRecyclerview.adapter = null
        super.onDestroyView()
    }

}