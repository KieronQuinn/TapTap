package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.ActionDataTypes
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.ActionSupportedRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.shared.SharedArgument
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorFragment.Companion.FRAGMENT_EXTRA_SHOW_ALL_APPS
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorFragment.Companion.FRAGMENT_EXTRA_TITLE
import com.kieronquinn.app.taptap.utils.extensions.Shell_isRooted
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dinglisch.android.tasker.TaskerIntent

abstract class SettingsActionsAddGenericViewModel: ViewModel() {

    abstract fun showAppShortcutPicker()
    abstract fun showShortcutPicker()
    abstract fun showShizukuPermission(action: TapTapActionDirectory)
    abstract fun showSnapchatFlow(action: TapTapActionDirectory)
    abstract fun showAppPicker(action: TapTapActionDirectory)
    abstract fun showTaskerTaskPicker(contract: ActivityResultLauncher<Intent>)
    abstract fun showQuickSettingPicker()
    abstract fun showNoRoot()
    abstract fun unwindToActions()
    abstract suspend fun checkRoot(): Boolean
    abstract fun getFormattedDescriptionForAction(context: Context, action: TapTapActionDirectory, data: String?): CharSequence
    abstract fun isActionDataSatisfied(context: Context, data: ActionDataTypes, extraData: String): Boolean
    abstract fun getActionSupportedRequirement(context: Context, action: TapTapActionDirectory): ActionSupportedRequirement?

}

abstract class SettingsActionsAddGenericViewModelImpl(private val navigation: ContainerNavigation, private val actionsRepository: ActionsRepository): SettingsActionsAddGenericViewModel() {

    override fun unwindToActions() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_add_action, true)
        }
    }

    override fun showAppShortcutPicker() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_picker_app_shortcut)
        }
    }

    override fun showShortcutPicker() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_picker_shortcut)
        }
    }

    override fun showShizukuPermission(action: TapTapActionDirectory) {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_shizuku_permission_flow, SharedArgument(action = action).toBundle())
        }
    }

    override fun showSnapchatFlow(action: TapTapActionDirectory) {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_snapchat, SharedArgument(action = action).toBundle())
        }
    }

    override fun showAppPicker(action: TapTapActionDirectory) {
        viewModelScope.launch {
            val bundle = SharedArgument(action = action).toBundle().apply {
                putBoolean(FRAGMENT_EXTRA_SHOW_ALL_APPS, false)
                putInt(FRAGMENT_EXTRA_TITLE, R.string.action_launch_app)
            }
            navigation.navigate(R.id.action_global_nav_graph_shared_picker_package, bundle)
        }
    }

    override fun showTaskerTaskPicker(contract: ActivityResultLauncher<Intent>) {
        contract.launch(TaskerIntent.getTaskSelectIntent())
    }

    override suspend fun checkRoot(): Boolean {
        return withContext(Dispatchers.IO){
            Shell_isRooted()
        }
    }

    override fun showQuickSettingPicker() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_picker_quick_setting)
        }
    }

    override fun showNoRoot() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_no_root)
        }
    }

    override fun isActionDataSatisfied(context: Context, data: ActionDataTypes, extraData: String): Boolean {
        return actionsRepository.isActionDataSatisfied(context, data, extraData)
    }

    override fun getActionSupportedRequirement(context: Context, action: TapTapActionDirectory): ActionSupportedRequirement? {
        return actionsRepository.getUnsupportedReason(context, action)
    }

    override fun getFormattedDescriptionForAction(
        context: Context,
        action: TapTapActionDirectory,
        data: String?
    ): CharSequence {
        return actionsRepository.getFormattedDescriptionForAction(context, action, data)
    }

}