package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter
import com.kieronquinn.app.taptap.models.gate.GateDataTypes
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapUIGate
import com.kieronquinn.app.taptap.models.shared.ARG_NAME_SHARED_ARGUMENT
import com.kieronquinn.app.taptap.models.shared.SharedArgument
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorFragment
import com.kieronquinn.app.taptap.ui.screens.settings.shared.shizuku.SettingsSharedShizukuPermissionFlowFragment
import com.kieronquinn.app.taptap.utils.extensions.getAccessibilityIntent
import com.kieronquinn.app.taptap.utils.extensions.getAppInfoIntent
import com.kieronquinn.app.taptap.utils.extensions.getPermissionName
import com.kieronquinn.app.taptap.utils.extensions.getRequiredPermissions
import com.kieronquinn.app.taptap.utils.extensions.isPermissionDenied
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class SettingsGatesAddGenericFragment<T : ViewBinding>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T) :
    BoundFragment<T>(inflate) {

    companion object {
        const val FRAGMENT_RESULT_KEY_GATE = "fragment_result_gate"
    }

    abstract val viewModel: SettingsGatesAddGenericViewModel

    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()
    private val accessibilityRouter by inject<TapTapAccessibilityRouter>()

    private val onResume = MutableSharedFlow<Unit>()
    private val onShizukuPermissionResponse = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val permissionResponse = MutableSharedFlow<Map<String, Boolean>>()
    private val permissionResponseContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        whenResumed {
            permissionResponse.emit(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupResultListeners()
        accessibilityRouter.bringToFrontOnAccessibilityStart(this)
    }

    protected fun onGateClicked(gate: TapTapGateDirectory) = whenResumed {
        handleGate(gate)
    }

    private suspend fun handleGate(gate: TapTapGateDirectory, extraData: String? = null, isReturningData: Boolean = false, isReturningRequirement: Boolean = false){
        if(!handleGateRequirement(gate, isReturningRequirement)) return //Permission rejected
        if(gate.dataType != null && !viewModel.isGateDataSatisfied(requireContext(), gate.dataType, extraData ?: "")){
            if(isReturningData) return //User has returned invalid data
            handleGateData(gate)
            return
        }
        val description = viewModel.getFormattedDescriptionForGate(requireContext(), gate, extraData)
        //Index & id are handled later
        val uiGate = TapTapUIGate(-1, gate, true,-1, extraData ?: "", description)
        setFragmentResult(FRAGMENT_RESULT_KEY_GATE, bundleOf(FRAGMENT_RESULT_KEY_GATE to uiGate))
        viewModel.unwindToGates()
    }

    private suspend fun handleGateRequirement(gate: TapTapGateDirectory, isReturning: Boolean): Boolean {
        if(gate.gateRequirement == null) return true
        return gate.gateRequirement.all { requirement ->
            when(requirement){
                is GateRequirement.ReadPhoneStatePermission -> {
                    requestPermission(Manifest.permission.READ_PHONE_STATE)
                }
                is GateRequirement.Accessibility -> {
                    requestAccessibilityService()
                }
                is GateRequirement.Shizuku -> {
                    return if(!isReturning){
                        viewModel.showShizukuPermission(gate)
                        false
                    }else true
                }
                is GateRequirement.UserDisplayedGateRequirement, is GateRequirement.Permission -> throw NotImplementedError("Requirement $requirement is not implemented")
            }
        }
    }

    private fun handleGateData(gate: TapTapGateDirectory) {
        when(gate.dataType){
            GateDataTypes.PACKAGE_NAME -> {
                //Package picker handles gate result itself
                viewModel.showAppPicker(gate)
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

    private suspend fun requestAccessibilityService(): Boolean {
        if(requireContext().isServiceRunning(TapTapAccessibilityService::class.java)) return true
        //Launch accessibility settings
        startActivity(requireContext().getAccessibilityIntent(TapTapAccessibilityService::class.java))
        //Await onResume, then we can return to checking again
        onResume.take(1).first()
        return requireContext().isServiceRunning(TapTapAccessibilityService::class.java)
    }

    protected fun showSnackbarForChip(requirement: GateRequirement.UserDisplayedGateRequirement) = whenResumed {
        sharedViewModel.showSnackbar(getText(requirement.desc))
    }

    private fun setupResultListeners() {
        setFragmentResultListener(SettingsSharedPackageSelectorFragment.FRAGMENT_RESULT_KEY_PACKAGE) { key, bundle ->
            val action = bundle.getParcelable<SharedArgument>(ARG_NAME_SHARED_ARGUMENT)?.gate ?: return@setFragmentResultListener
            val packageName = bundle.getString(SettingsSharedPackageSelectorFragment.FRAGMENT_RESULT_KEY_PACKAGE) ?: return@setFragmentResultListener
            whenResumed {
                handleGate(action, packageName, isReturningRequirement = true)
            }
        }
        setFragmentResultListener(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION) { key, bundle ->
            val permissionGranted = bundle.getBoolean(SettingsSharedShizukuPermissionFlowFragment.FRAGMENT_RESULT_KEY_SHIZUKU_PERMISSION, false)
            val gate = bundle.getParcelable<SharedArgument>(ARG_NAME_SHARED_ARGUMENT)?.gate ?: return@setFragmentResultListener
            whenResumed {
                if(permissionGranted) {
                    handleGate(gate, isReturningRequirement = true)
                } //Drop if permission is denied
            }
        }
    }

    override fun onResume() {
        super.onResume()
        whenResumed {
            onResume.emit(Unit)
        }
    }

}