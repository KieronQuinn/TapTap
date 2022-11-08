package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.databinding.FragmentSettingsBackupRestoreRestoreBinding
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.service.accessibility.TapTapGestureAccessibilityService
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel.FabState
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel.ResolvedRequirement
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel.State
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.snapchat.SettingsSharedSnapchatFragment
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsBackupRestoreRestoreFragment :
    BoundFragment<FragmentSettingsBackupRestoreRestoreBinding>(
        FragmentSettingsBackupRestoreRestoreBinding::inflate
    ), BackAvailable, ProvidesBack {

    private val viewModel by viewModel<SettingsBackupRestoreRestoreViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val args by navArgs<SettingsBackupRestoreRestoreFragmentArgs>()
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()
    private val adapter by lazy {
        SettingsBackupRestoreRestoreAdapter(
            binding.settingsBackupRestoreRestoreRecyclerview,
            emptyList(),
            ::onSetupClicked,
            viewModel::onRequirementSkipped
        )
    }

    private val onResume = MutableSharedFlow<Unit>()
    private val permissionResponse = MutableSharedFlow<Map<String, Boolean>>()
    private val permissionResponseContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            permissionResponse.emit(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupRecyclerView()
        setupState()
        setupFab()
        setupResultListeners()
        setupClose()
        accessibilityRouter.bringToFrontOnAccessibilityStart(this)
        accessibilityRouter.bringToFrontOnGestureAccessibilityStart(this)
        viewModel.setUri(args.uri)
    }

    override fun onBackPressed(): Boolean {
        return if(viewModel.state.value is State.Finished){
            viewModel.onCloseClicked()
            true
        }else false
    }

    private fun setupMonet() {
        binding.settingsBackupRestoreRestoreProgress.applyMonet()
        binding.settingsBackupRestoreRestoreIcon.imageTintList = ColorStateList.valueOf(monet.getAccentColor(requireContext()))
        binding.settingsBackupRestoreRestoreClose.setTextColor(monet.getAccentColor(requireContext()))
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.settingsBackupRestoreRestoreRecyclerview) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@SettingsBackupRestoreRestoreFragment.adapter
        applyBottomInsets(binding.root, resources.getDimension(R.dimen.container_fab_margin).toInt())
    }

    private fun setupFab() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.fabClicked.collect {
            if (it != FabState.FabAction.RESTORE_BACKUP) return@collect
            viewModel.onFabClicked()
        }
    }

    private fun setupClose() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsBackupRestoreRestoreClose.onClicked().collect {
            viewModel.onCloseClicked()
        }
    }

    private fun handleState(state: State) {
        when (state) {
            is State.Loading -> {
                setFabState(false)
                binding.settingsBackupRestoreRestoreProgress.isVisible = true
                binding.settingsBackupRestoreRestoreTitle.isVisible = true
                binding.settingsBackupRestoreRestoreDesc.isVisible = true
                binding.settingsBackupRestoreRestoreClose.isVisible = false
                binding.settingsBackupRestoreRestoreIcon.isVisible = false
                binding.settingsBackupRestoreRestoreRecyclerview.isVisible = false
                binding.settingsBackupRestoreRestoreTitle.setText(R.string.settings_backuprestore_restore_title)
                binding.settingsBackupRestoreRestoreDesc.setText(R.string.settings_backuprestore_restore_desc)
            }
            is State.ActionRequired -> {
                setFabState(state.isContinueEnabled)
                binding.settingsBackupRestoreRestoreProgress.isVisible = false
                binding.settingsBackupRestoreRestoreTitle.isVisible = false
                binding.settingsBackupRestoreRestoreDesc.isVisible = false
                binding.settingsBackupRestoreRestoreClose.isVisible = false
                binding.settingsBackupRestoreRestoreIcon.isVisible = false
                binding.settingsBackupRestoreRestoreRecyclerview.isVisible = true
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
            is State.Restoring -> {
                setFabState(false)
                binding.settingsBackupRestoreRestoreProgress.isVisible = true
                binding.settingsBackupRestoreRestoreTitle.isVisible = true
                binding.settingsBackupRestoreRestoreDesc.isVisible = true
                binding.settingsBackupRestoreRestoreClose.isVisible = false
                binding.settingsBackupRestoreRestoreIcon.isVisible = false
                binding.settingsBackupRestoreRestoreRecyclerview.isVisible = false
                binding.settingsBackupRestoreRestoreTitle.setText(R.string.settings_backuprestore_restore_title)
                binding.settingsBackupRestoreRestoreDesc.setText(R.string.settings_backuprestore_restore_desc)
            }
            is State.Finished -> {
                setFabState(false)
                binding.settingsBackupRestoreRestoreTitle.isVisible = true
                binding.settingsBackupRestoreRestoreDesc.isVisible = true
                binding.settingsBackupRestoreRestoreClose.isVisible = true
                binding.settingsBackupRestoreRestoreIcon.isVisible = true
                binding.settingsBackupRestoreRestoreProgress.isVisible = false
                binding.settingsBackupRestoreRestoreRecyclerview.isVisible = false
                if (state.success) {
                    binding.settingsBackupRestoreRestoreIcon.setImageResource(R.drawable.ic_check_circle)
                    binding.settingsBackupRestoreRestoreTitle.setText(R.string.settings_backuprestore_restore_done_title)
                    binding.settingsBackupRestoreRestoreDesc.text =
                        binding.root.context.getString(
                            R.string.settings_backuprestore_restore_done_desc,
                            state.filename ?: ""
                        )
                } else {
                    binding.settingsBackupRestoreRestoreIcon.setImageResource(R.drawable.ic_error_circle)
                    binding.settingsBackupRestoreRestoreTitle.setText(R.string.settings_backuprestore_restore_error_title)
                    binding.settingsBackupRestoreRestoreDesc.text =
                        binding.root.context.getString(R.string.settings_backuprestore_restore_error_desc)
                }
            }
        }
    }

    private fun setFabState(showFab: Boolean) {
        sharedViewModel.setFabState(if (showFab) FabState.Shown(FabState.FabAction.RESTORE_BACKUP) else FabState.Hidden)
    }

    private fun onSetupClicked(item: SettingsBackupRestoreRestoreViewModel.Item.Requirement) = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        //Take the first requirement as the list is dynamic
        val requirement = item.requirements.firstOrNull() ?: return@launchWhenResumed
        when (requirement) {
            is ResolvedRequirement.Action -> handleSetupActionRequirement(
                requirement.requirement
            )
            is ResolvedRequirement.Gate -> handleSetupGateRequirement(
                requirement.requirement
            )
        }
    }

    private suspend fun handleSetupActionRequirement(requirement: ActionRequirement) {
        val result = when(requirement){
            is ActionRequirement.Accessibility -> requestAccessibilityService()
            is ActionRequirement.AnswerPhoneCallsPermission -> requestPermission(Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_STATE)
            is ActionRequirement.GestureAccessibility -> requestGestureService()
            is ActionRequirement.TaskerPermission -> requestTaskerPermission()
            is ActionRequirement.AccessNotificationPolicyPermission -> requestNotificationPolicy()
            is ActionRequirement.CameraPermission -> requestPermission(Manifest.permission.CAMERA)
            is ActionRequirement.DrawOverOtherAppsPermission -> requestDisplayOverOtherApps()
            is ActionRequirement.WriteSystemSettingsPermission -> requestWriteSystemSettings()
            is ActionRequirement.Shizuku -> {
                viewModel.launchShizukuFlow(false)
                false
            }
            is ActionRequirement.Root -> {
                val isRooted = viewModel.checkRoot()
                if(!isRooted){
                    viewModel.showNoRoot()
                }
                isRooted
            }
            is ActionRequirement.Snapchat -> {
                viewModel.launchSnapchatFlow()
                false
            }
            else -> false
        }
        if(result){
            viewModel.onRequirementResolved(ResolvedRequirement.Action(requirement))
        }
    }

    private suspend fun handleSetupGateRequirement(requirement: GateRequirement) {
        val result = when(requirement){
            is GateRequirement.Accessibility -> requestAccessibilityService()
            is GateRequirement.ReadPhoneStatePermission -> requestPermission(Manifest.permission.READ_PHONE_STATE)
            is GateRequirement.Shizuku -> {
                viewModel.launchShizukuFlow(true)
                false
            }
            else -> false
        }
        if(result){
            viewModel.onRequirementResolved(ResolvedRequirement.Gate(requirement))
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

    private suspend fun requestWriteSystemSettings(): Boolean {
        if(Settings.System.canWrite(context)) return true
        //Launch write system settings page
        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        })
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return Settings.System.canWrite(context)
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

    private fun setupResultListeners() {
        setFragmentResultListener(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION) { key, bundle ->
            val permissionGranted = bundle.getBoolean(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION, false)
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if(permissionGranted) {
                    viewModel.onRequirementResolved(ResolvedRequirement.Action(ActionRequirement.Shizuku))
                } //Drop if permission is denied
            }
        }
        setFragmentResultListener(SettingsSharedSnapchatFragment.FRAGMENT_RESULT_KEY_SNAPCHAT) { key, bundle ->
            val available = bundle.getBoolean(SettingsSharedSnapchatFragment.FRAGMENT_RESULT_KEY_SNAPCHAT, false)
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if(available) {
                    viewModel.onRequirementResolved(ResolvedRequirement.Action(ActionRequirement.Snapchat))
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