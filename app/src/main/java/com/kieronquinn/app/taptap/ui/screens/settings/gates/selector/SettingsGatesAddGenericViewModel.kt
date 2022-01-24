package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.gate.GateDataTypes
import com.kieronquinn.app.taptap.models.gate.GateSupportedRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.shared.SharedArgument
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorFragment
import kotlinx.coroutines.launch

abstract class SettingsGatesAddGenericViewModel: ViewModel() {

    abstract fun showShizukuPermission(gate: TapTapGateDirectory)
    abstract fun showAppPicker(gate: TapTapGateDirectory)
    abstract fun unwindToGates()
    abstract fun getFormattedDescriptionForGate(context: Context, gate: TapTapGateDirectory, data: String?): CharSequence
    abstract fun isGateDataSatisfied(context: Context, data: GateDataTypes, extraData: String): Boolean
    abstract fun getGateSupportedRequirement(context: Context, gate: TapTapGateDirectory): GateSupportedRequirement?

}

abstract class SettingsGatesAddGenericViewModelImpl(private val navigation: ContainerNavigation, private val gatesRepository: GatesRepository): SettingsGatesAddGenericViewModel() {

    override fun unwindToGates() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_add_gate, true)
        }
    }

    override fun showShizukuPermission(gate: TapTapGateDirectory) {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_shizuku_permission_flow, SharedArgument(gate = gate).toBundle())
        }
    }

    override fun showAppPicker(gate: TapTapGateDirectory) {
        viewModelScope.launch {
            val bundle = SharedArgument(gate = gate).toBundle().apply {
                putBoolean(SettingsSharedPackageSelectorFragment.FRAGMENT_EXTRA_SHOW_ALL_APPS, true)
                putInt(SettingsSharedPackageSelectorFragment.FRAGMENT_EXTRA_TITLE, R.string.action_launch_app)
            }
            navigation.navigate(R.id.action_global_nav_graph_shared_picker_package, bundle)
        }
    }

    override fun getGateSupportedRequirement(context: Context, gate: TapTapGateDirectory): GateSupportedRequirement? {
        return gatesRepository.getGateSupportedRequirement(context, gate)
    }

    override fun getFormattedDescriptionForGate(
        context: Context,
        gate: TapTapGateDirectory,
        data: String?
    ): CharSequence {
        return gatesRepository.getFormattedDescriptionForGate(context, gate, data)
    }

    override fun isGateDataSatisfied(
        context: Context,
        data: GateDataTypes,
        extraData: String
    ): Boolean {
        return gatesRepository.isGateDataSatisfied(context, data, extraData)
    }

}