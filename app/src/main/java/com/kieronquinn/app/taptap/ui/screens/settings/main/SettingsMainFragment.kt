package com.kieronquinn.app.taptap.ui.screens.settings.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsMainBinding
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.CanShowSnackbar
import com.kieronquinn.app.taptap.ui.base.ProvidesOverflow
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsMainFragment :
    BoundFragment<FragmentSettingsMainBinding>(FragmentSettingsMainBinding::inflate), CanShowSnackbar, ProvidesOverflow {

    private val viewModel by viewModel<SettingsMainViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val adapter by lazy {
        SettingsMainAdapter()
    }

    private val items by lazy {
        listOf(
            SettingsItem.Info(
                icon = R.drawable.ic_settings_battery_optimisation,
                contentRes = R.string.settings_main_battery,
                isVisible = { !viewModel.batteryOptimisationDisabled },
                onClick = viewModel::onBatteryOptimisationClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_actions,
                titleRes = R.string.setting_actions_double,
                contentRes = R.string.settings_actions_desc_double,
                onClick = viewModel::onDoubleTapActionsClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_actions_triple,
                titleRes = R.string.setting_actions_triple,
                contentRes = R.string.settings_actions_desc_triple,
                onClick = viewModel::onTripleTapActionsClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_gates,
                titleRes = R.string.setting_gates,
                contentRes = R.string.setting_gates_desc,
                onClick = viewModel::onGatesClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_feedback,
                titleRes = R.string.setting_feedback,
                contentRes = R.string.setting_feedback_desc,
                onClick = viewModel::onFeedbackClicked
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwitch()
        setupRecyclerView()
        viewModel.checkInternetPermission()
    }

    private fun setupSwitch() {
        binding.settingsMainEnableTapTap.isChecked = sharedViewModel.isServiceRunning.value
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            with(binding.settingsMainEnableTapTap) {
                launch {
                    sharedViewModel.isServiceRunning.collect {
                        isChecked = it
                    }
                }
                launch {
                    onClicked().collect {
                        sharedViewModel.toggleServiceEnabledState(it.context)
                    }
                }
            }
        }
    }


    private fun setupRecyclerView() = with(binding.settingsMainRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsMainFragment.adapter
        applyBottomInsets(binding.root)
    }

    override fun onDestroyView() {
        binding.settingsMainRecyclerView.adapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        adapter.refreshVisibleItems()
    }

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.menu_settings, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.menu_settings_rerun_setup -> {
                viewModel.onReRunSetupClicked()
                return true
            }
        }
        return false
    }

    inner class SettingsMainAdapter: GenericSettingsAdapter(requireContext(), binding.settingsMainRecyclerView, items)
}