package com.kieronquinn.app.taptap.ui.screens.settings.actions.doubletap

import com.kieronquinn.app.taptap.databinding.FragmentSettingsActionsDoubleBinding
import com.kieronquinn.app.taptap.repositories.room.actions.DoubleTapAction
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActionsDoubleFragment: SettingsActionsGenericFragment<FragmentSettingsActionsDoubleBinding, DoubleTapAction>(FragmentSettingsActionsDoubleBinding::inflate), LockCollapsed, BackAvailable {

    override val viewModel by viewModel<SettingsActionsDoubleViewModel>()

    override fun getLoadingProgressView() = binding.settingsActionsDoubleLoadingProgress
    override fun getLoadingView() = binding.settingsActionsDoubleLoading
    override fun getRecyclerView() = binding.settingsActionsDoubleRecyclerview
    override fun getEmptyView() = binding.settingsActionsDoubleEmpty

}