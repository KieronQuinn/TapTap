package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.components.columbus.actions.custom.LaunchAppShortcutAction
import com.kieronquinn.app.taptap.models.action.ActionDataTypes
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.models.columbus.AppShortcutData
import com.kieronquinn.app.taptap.models.shared.ARG_NAME_SHARED_ARGUMENT
import com.kieronquinn.app.taptap.models.shared.SharedArgument
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.service.accessibility.TapTapGestureAccessibilityService
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts.SettingsSharedAppShortcutsSelectorFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting.SettingsSharedQuickSettingSelectorFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts.SettingsSharedShortcutsSelectorFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat.SettingsSharedSnapchatFragment
import com.kieronquinn.app.taptap.utils.extensions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class SettingsActionsAddGenericFragment<T : ViewBinding>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T) :
    BoundFragment<T>(inflate) {

    companion object {
        const val FRAGMENT_RESULT_KEY_ACTION = "fragment_result_action"
    }

    abstract val viewModel: SettingsActionsAddGenericViewModel

    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val gson by inject<Gson>()
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()

    private val onResume = MutableSharedFlow<Unit>()
    private val permissionResponse = MutableSharedFlow<Map<String, Boolean>>()
    private val permissionResponseContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            permissionResponse.emit(it)
        }
    }
    private val taskerResponseContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val taskName = it.data?.dataString ?: return@launchWhenResumed //Drop if task not picked
            handleAction(TapTapActionDirectory.TASKER_TASK, taskName, isReturningData = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupResultListeners()
        accessibilityRouter.bringToFrontOnAccessibilityStart(this)
        accessibilityRouter.bringToFrontOnGestureAccessibilityStart(this)
    }

    protected fun onActionClicked(action: TapTapActionDirectory) = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        handleAction(action)
    }

    private suspend fun handleAction(action: TapTapActionDirectory, extraData: String? = null, isReturningData: Boolean = false, isReturningRequirement: Boolean = false){
        if(!handleActionRequirement(action, isReturningRequirement)) return //Permission rejected
        if(action.dataType != null && !viewModel.isActionDataSatisfied(requireContext(), action.dataType, extraData ?: "")){
            if(isReturningData) return //User has returned invalid data
            handleActionData(action)
            return
        }
        val description = viewModel.getFormattedDescriptionForAction(requireContext(), action, extraData)
        //Index & id are handled later
        val uiAction = TapTapUIAction(action, -1, -1, extraData ?: "", description, 0)
        setFragmentResult(FRAGMENT_RESULT_KEY_ACTION, bundleOf(FRAGMENT_RESULT_KEY_ACTION to uiAction))
        viewModel.unwindToActions()
    }

    private suspend fun handleActionRequirement(action: TapTapActionDirectory, isReturning: Boolean): Boolean {
        if(action.actionRequirement == null) return true
        return action.actionRequirement.all { requirement ->
            when(requirement){
                is ActionRequirement.AnswerPhoneCallsPermission -> {
                    requestPermission(Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_STATE)
                }
                is ActionRequirement.CameraPermission -> {
                    requestPermission(Manifest.permission.CAMERA)
                }
                is ActionRequirement.AccessNotificationPolicyPermission -> {
                    requestNotificationPolicy()
                }
                is ActionRequirement.Accessibility -> {
                    requestAccessibilityService()
                }
                is ActionRequirement.GestureAccessibility -> {
                    requestGestureService()
                }
                is ActionRequirement.DrawOverOtherAppsPermission -> {
                    requestDisplayOverOtherApps()
                }
                is ActionRequirement.TaskerPermission -> {
                    requestTaskerPermission()
                }
                is ActionRequirement.Shizuku -> {
                    return if(!isReturning){
                        viewModel.showShizukuPermission(action)
                        false
                    }else true
                }
                is ActionRequirement.Snapchat -> {
                    return if(!isReturning) {
                        viewModel.showSnapchatFlow(action)
                        false
                    }else true
                }
                is ActionRequirement.Root -> {
                    val isRooted = viewModel.checkRoot()
                    if(!isRooted){
                        viewModel.showNoRoot()
                    }
                    isRooted
                }
                is ActionRequirement.Tasker -> {
                    //Already passed
                    true
                }
                is ActionRequirement.UserDisplayedActionRequirement, is ActionRequirement.Permission -> throw NotImplementedError("Requirement $requirement is not implemented")
            }
        }
    }

    private fun handleActionData(action: TapTapActionDirectory) {
        when(action.dataType){
            ActionDataTypes.PACKAGE_NAME -> {
                //Package picker handles action result itself
                viewModel.showAppPicker(action)
            }
            ActionDataTypes.APP_SHORTCUT -> {
                //App shortcut picker handles action result itself
                viewModel.showAppShortcutPicker()
            }
            ActionDataTypes.SHORTCUT -> {
                //Shortcut picker handles action result itself
                viewModel.showShortcutPicker()
            }
            ActionDataTypes.TASKER_TASK -> {
                //Tasker picker handles action result itself
                viewModel.showTaskerTaskPicker(taskerResponseContract)
            }
            ActionDataTypes.QUICK_SETTING -> {
                //Quick Setting picker handles action result itself
                viewModel.showQuickSettingPicker()
            }
            else -> {}
        }
    }

    /**
     *  Requests all the permissions, either by opening the settings and awaiting a resume
     *  or by using runtime permissions if possible. If all permissions are already granted,
     *  the method will skip requesting.
     */
    private suspend fun requestPermission(vararg permission: String): Boolean {
        //Get required permissions
        val requiredPermissions = requireContext().getRequiredPermissions(*permission)
        if(requiredPermissions.isEmpty()){
            //Not required to request
            return true
        }
        //Check for denied permissions first as they require manual granting
        requiredPermissions.forEach {
            if(requireActivity().isPermissionDenied(it)){
                //Show a toast to inform the user they need to grant a permission
                Toast.makeText(requireContext(), getString(R.string.permission_toast, requireContext().getPermissionName(it)), Toast.LENGTH_LONG).show()
                //Toasts are hidden by launches to allow it to show for a little bit
                delay(250L)
                //Launch the settings
                startActivity(requireContext().getAppInfoIntent())
                //Await resume
                onResume.take(1).first()
                if(requireActivity().isPermissionDenied(it)){
                    //Permission still not granted, we can't add this action
                    return false
                }
            }
        }
        //Request runtime permission
        permissionResponseContract.launch(requiredPermissions)
        return permissionResponse.take(1).first().all { it.value }
    }

    private suspend fun requestNotificationPolicy(): Boolean {
        if(requireContext().doesHaveNotificationPolicyAccess()) return true
        //Launch notification policy settings
        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return requireContext().doesHaveNotificationPolicyAccess()
    }

    private suspend fun requestDisplayOverOtherApps(): Boolean {
        if(Settings.canDrawOverlays(requireContext())) return true
        //Show a toast to inform the user they need to grant a permission
        Toast.makeText(requireContext(), getString(R.string.permission_toast_draw_over_apps), Toast.LENGTH_LONG).show()
        //Toasts are hidden by launches to allow it to show for a little bit
        delay(250L)
        //Launch notification policy settings
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        })
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return Settings.canDrawOverlays(requireContext())
    }

    private suspend fun requestAccessibilityService(): Boolean {
        if(requireContext().isServiceRunning(TapTapAccessibilityService::class.java)) return true
        //Launch accessibility settings
        startActivity(requireContext().getAccessibilityIntent(TapTapAccessibilityService::class.java))
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return requireContext().isServiceRunning(TapTapAccessibilityService::class.java)
    }

    private suspend fun requestGestureService(): Boolean {
        if(requireContext().isServiceRunning(TapTapGestureAccessibilityService::class.java)) return true
        //Launch accessibility settings
        startActivity(requireContext().getAccessibilityIntent(TapTapGestureAccessibilityService::class.java))
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return requireContext().isServiceRunning(TapTapGestureAccessibilityService::class.java)
    }

    private suspend fun requestTaskerPermission(): Boolean {
        if(requireContext().doesHaveTaskerPermission()) return true
        Toast.makeText(requireContext(), R.string.tasker_content_toast, Toast.LENGTH_LONG).show()
        delay(250L)
        Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //This seems to set the current tab. Misc is the 4th tab.
            putExtra("tno", 3)
        }.also {
            startActivity(it)
        }
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return requireContext().doesHaveTaskerPermission()
    }

    protected fun showSnackbarForChip(requirement: ActionRequirement.UserDisplayedActionRequirement) = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.showSnackbar(getText(requirement.desc))
    }

    private fun setupResultListeners() {
        setFragmentResultListener(SettingsSharedAppShortcutsSelectorFragment.FRAGMENT_RESULT_KEY_APP_SHORTCUT) { key, bundle ->
            val selectedAppShortcut = bundle.getParcelable<AppShortcutData>(
                SettingsSharedAppShortcutsSelectorFragment.FRAGMENT_RESULT_KEY_APP_SHORTCUT) ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                handleAction(TapTapActionDirectory.LAUNCH_APP_SHORTCUT, gson.toJson(selectedAppShortcut), isReturningRequirement = true)
            }
        }
        setFragmentResultListener(SettingsSharedShortcutsSelectorFragment.FRAGMENT_RESULT_KEY_SHORTCUT) { key, bundle ->
            val serializedShortcut = bundle.getString(SettingsSharedShortcutsSelectorFragment.FRAGMENT_RESULT_KEY_SHORTCUT) ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                handleAction(TapTapActionDirectory.LAUNCH_SHORTCUT, serializedShortcut, isReturningRequirement = true)
            }
        }
        setFragmentResultListener(SettingsSharedQuickSettingSelectorFragment.FRAGMENT_RESULT_KEY_QUICK_SETTING) { key, bundle ->
            val serializedTile = bundle.getParcelable<QuickSettingsRepository.QuickSetting>(SettingsSharedQuickSettingSelectorFragment.FRAGMENT_RESULT_KEY_QUICK_SETTING) ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                handleAction(TapTapActionDirectory.QUICK_SETTING, serializedTile.component.flattenToString(), isReturningRequirement = true)
            }
        }
        setFragmentResultListener(SettingsSharedPackageSelectorFragment.FRAGMENT_RESULT_KEY_PACKAGE) { key, bundle ->
            val action = bundle.getParcelable<SharedArgument>(ARG_NAME_SHARED_ARGUMENT)?.action ?: return@setFragmentResultListener
            val packageName = bundle.getString(SettingsSharedPackageSelectorFragment.FRAGMENT_RESULT_KEY_PACKAGE) ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                handleAction(action, packageName, isReturningRequirement = true)
            }
        }
        setFragmentResultListener(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION) { key, bundle ->
            val permissionGranted = bundle.getBoolean(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION, false)
            val action = bundle.getParcelable<SharedArgument>(ARG_NAME_SHARED_ARGUMENT)?.action ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if(permissionGranted) {
                    handleAction(action, isReturningRequirement = true)
                } //Drop if permission is denied
            }
        }
        setFragmentResultListener(SettingsSharedSnapchatFragment.FRAGMENT_RESULT_KEY_SNAPCHAT) { key, bundle ->
            val available = bundle.getBoolean(SettingsSharedSnapchatFragment.FRAGMENT_RESULT_KEY_SNAPCHAT, false)
            val action = bundle.getParcelable<SharedArgument>(ARG_NAME_SHARED_ARGUMENT)?.action ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if(available) {
                    handleAction(action, isReturningRequirement = true)
                } //Drop if not available
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            onResume.emit(Unit)
        }
    }

}