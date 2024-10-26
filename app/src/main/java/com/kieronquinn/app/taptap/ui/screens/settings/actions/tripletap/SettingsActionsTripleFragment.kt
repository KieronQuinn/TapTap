package com.kieronquinn.app.taptap.ui.screens.settings.actions.tripletap

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.databinding.FragmentSettingsActionsTripleBinding
import com.kieronquinn.app.taptap.repositories.room.actions.TripleTapAction
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericFragment
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActionsTripleFragment: SettingsActionsGenericFragment<FragmentSettingsActionsTripleBinding, TripleTapAction>(FragmentSettingsActionsTripleBinding::inflate), LockCollapsed, BackAvailable {

    override val viewModel by viewModel<SettingsActionsTripleViewModel>()

    override fun getLoadingProgressView() = binding.settingsActionsTripleLoadingProgress
    override fun getLoadingView() = binding.settingsActionsTripleLoading
    override fun getRecyclerView() = binding.settingsActionsTripleRecyclerview
    override fun getEmptyView() = binding.settingsActionsTripleEmpty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwitchState()
        setupSwitchClicked()
    }

    private fun setupSwitchState() {
        binding.settingsActionsTripleSwitch.isChecked = viewModel.tripleTapEnabled.getSync()
        whenResumed {
            viewModel.tripleTapEnabled.asFlow().collect {
                binding.settingsActionsTripleSwitch.isChecked = it
            }
        }
    }

    private fun setupSwitchClicked() = whenResumed {
        binding.settingsActionsTripleSwitch.onClicked().collect {
            viewModel.onTripleTapSwitchClicked()
        }
    }

}