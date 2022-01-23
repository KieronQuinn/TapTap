package com.kieronquinn.app.taptap.ui.screens.settings.generic

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsGenericBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onApplyInsets
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class GenericSettingsFragment: BoundFragment<FragmentSettingsGenericBinding>(FragmentSettingsGenericBinding::inflate) {

    abstract val viewModel: GenericSettingsViewModel
    abstract val items: List<SettingsItem>
    protected val containerViewModel by sharedViewModel<ContainerSharedViewModel>()

    abstract fun createAdapter(items: List<SettingsItem>): GenericSettingsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRestart()
    }

    private fun setupRestart() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.restartService?.collect {
                containerViewModel.restartService(requireContext())
            }
        }
    }

    private fun setupRecyclerView() = with(binding.root) {
        layoutManager = LinearLayoutManager(context)
        adapter = createAdapter(items)
        applyBottomInsets(binding.root)
    }

    override fun onDestroyView() {
        binding.settingsGenericRecyclerView.adapter = null
        super.onDestroyView()
    }

}