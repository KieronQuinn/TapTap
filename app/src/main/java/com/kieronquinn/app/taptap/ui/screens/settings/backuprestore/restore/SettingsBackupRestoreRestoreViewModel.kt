package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.backup.Backup
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.shared.SharedArgument
import com.kieronquinn.app.taptap.repositories.backuprestore.RestoreRepository
import com.kieronquinn.app.taptap.utils.extensions.Shell_isRooted
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

abstract class SettingsBackupRestoreRestoreViewModel : ViewModel() {

    abstract fun setUri(uri: Uri)
    abstract val state: StateFlow<State>
    abstract fun onRequirementResolved(requirement: ResolvedRequirement)
    abstract fun onRequirementSkipped(uuid: String)
    abstract fun onFabClicked()
    abstract fun onCloseClicked()
    abstract fun launchSnapchatFlow()
    abstract fun launchShizukuFlow(isGate: Boolean)
    abstract suspend fun checkRoot(): Boolean
    abstract fun showNoRoot()

    sealed class State {
        object Loading : State()
        data class ActionRequired(val candidate: RestoreRepository.RestoreCandidate, val items: List<Item>, val isContinueEnabled: Boolean) : State()
        data class Restoring(val candidate: RestoreRepository.RestoreCandidate) : State()
        data class Finished(val success: Boolean, val filename: String?): State()
    }

    sealed class RestoreState {
        object Idle: RestoreState()
        data class Restore(val candidate: RestoreRepository.RestoreCandidate): RestoreState()
        data class Finished(val success: Boolean): RestoreState()
    }

    sealed class ResolvedRequirement {
        data class Action(val requirement: ActionRequirement): ResolvedRequirement()
        data class Gate(val requirement: GateRequirement): ResolvedRequirement()
    }

    sealed class PermissionsItem {
        data class Action(val action: Backup.Action) : PermissionsItem()
        data class Gate(val gate: Backup.Gate) : PermissionsItem()
        data class WhenGate(val gate: Backup.WhenGate) : PermissionsItem()
    }

    sealed class Item(val type: Type) {
        data class Infobox(@StringRes val content: Int) : Item(Type.INFOBOX)
        data class Header(@StringRes val title: Int) : Item(Type.HEADER)
        data class Requirement(
            val uuid: String,
            @DrawableRes val icon: Int,
            @StringRes val title: Int,
            @StringRes val desc: Int,
            @StringRes val chipText: Int,
            @DrawableRes val chipIcon: Int,
            val isGate: Boolean,
            val requirements: List<ResolvedRequirement>,
            val isSupported: Boolean
        ) : Item(Type.REQUIREMENT)

        enum class Type {
            INFOBOX, HEADER, REQUIREMENT
        }
    }

}

class SettingsBackupRestoreRestoreViewModelImpl(
    context: Context,
    private val restore: RestoreRepository,
    private val navigation: ContainerNavigation
) :
    SettingsBackupRestoreRestoreViewModel() {

    private val uri = MutableSharedFlow<Uri>()
    private val resolvedRequirement = MutableStateFlow<ResolvedRequirement?>(null)
    private val skippedRequirement = MutableStateFlow<String?>(null)
    private val restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle).apply {
        viewModelScope.launch {
            collect {
                if(it is RestoreState.Restore){
                    startRestore(it.candidate)
                }
            }
        }
    }

    private suspend fun startRestore(candidate: RestoreRepository.RestoreCandidate){
        restoreState.emit(RestoreState.Finished(restore.performRestore(candidate)))
    }

    //The FAB in this case acts as more like a switch - it skips any further showing of the actions
    private val fabClicked = MutableStateFlow(false)

    override fun setUri(uri: Uri) {
        viewModelScope.launch {
            this@SettingsBackupRestoreRestoreViewModelImpl.uri.emit(uri)
        }
    }

    override fun onFabClicked() {
        viewModelScope.launch {
            fabClicked.emit(true)
        }
    }

    private val restoreCandidate = uri.take(1).map {
        restore.loadRestoreCandidate(context, it)
    }

    override fun onRequirementResolved(requirement: ResolvedRequirement) {
        viewModelScope.launch {
            resolvedRequirement.emit(requirement)
        }
    }

    override fun onRequirementSkipped(uuid: String) {
        viewModelScope.launch {
            skippedRequirement.emit(uuid)
        }
    }

    override val state = combine(
        restoreCandidate,
        resolvedRequirement,
        skippedRequirement,
        fabClicked,
        restoreState
    ) { candidate, resolved, skipped, clicked, restore ->
        if(candidate == null) return@combine State.Finished(false, candidate?.filename)
        if(restore !is RestoreState.Idle){
            when(restore){
                is RestoreState.Restore -> return@combine State.Restoring(candidate)
                is RestoreState.Finished -> return@combine State.Finished(restore.success, candidate.filename)
                is RestoreState.Idle -> throw RuntimeException("Invalid state") //Cannot be idle
            }
        }
        if(clicked){
            restoreState.emit(RestoreState.Restore(candidate))
            return@combine State.Restoring(candidate)
        }
        if(resolved != null){
            candidate.resolveRequirement(resolved)
        }
        if(skipped != null){
            candidate.skipRequirement(skipped)
        }
        if(candidate.isFastTrackCapable()) {
            restoreState.emit(RestoreState.Restore(candidate))
            State.Restoring(candidate)
        }else{
            State.ActionRequired(candidate, createItems(candidate), candidate.areAllActionsResolved())
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun launchShizukuFlow(isGate: Boolean) {
        viewModelScope.launch {
            //The argument is purely for UI use here, we don't care about the argument in the result
            val argument = if(isGate){
                SharedArgument(gate = TapTapGateDirectory.APP_SHOWING)
            }else{
                SharedArgument(action = TapTapActionDirectory.LAUNCH_APP_SHORTCUT)
            }.toBundle()
            navigation.navigate(R.id.action_global_nav_graph_shared_shizuku_permission_flow, argument)
        }
    }

    override fun launchSnapchatFlow() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_snapchat,
                SharedArgument(action = TapTapActionDirectory.SNAPCHAT).toBundle())
        }
    }

    private fun RestoreRepository.RestoreCandidate.resolveRequirement(resolvedRequirement: ResolvedRequirement) {
        //Remove all cases of the requirement from the items
        val requirements = when(resolvedRequirement) {
            is ResolvedRequirement.Action -> resolvedRequirement.requirement
            is ResolvedRequirement.Gate -> resolvedRequirement.requirement
        }.run {
            //Handle shared requirements for both gates and actions
            val additionalRequirement = when(this){
                is GateRequirement.Accessibility -> ActionRequirement.Accessibility
                is ActionRequirement.Accessibility -> GateRequirement.Accessibility
                is ActionRequirement.AnswerPhoneCallsPermission -> GateRequirement.ReadPhoneStatePermission
                else -> null
            }
            arrayOf(this, additionalRequirement).filterNotNull()
        }
        doubleTapActions.forEach {
            it.actionRequirements = it.actionRequirements?.filterNot { requirements.contains(it) }?.toTypedArray()
        }
        tripleTapActions.forEach {
            it.actionRequirements = it.actionRequirements?.filterNot { requirements.contains(it) }?.toTypedArray()
        }
        gates.forEach {
            it.gateRequirements = it.gateRequirements?.filterNot { requirements.contains(it) }?.toTypedArray()
        }
        whenGatesDouble.forEach {
            it.gateRequirements = it.gateRequirements?.filterNot { requirements.contains(it) }?.toTypedArray()
        }
        whenGatesTriple.forEach {
            it.gateRequirements = it.gateRequirements?.filterNot { requirements.contains(it) }?.toTypedArray()
        }
    }

    private fun RestoreRepository.RestoreCandidate.skipRequirement(uuid: String) {
        doubleTapActions.find { it.uuid == uuid }?.let {
            it.skipped = true
            return
        }
        tripleTapActions.find { it.uuid == uuid }?.let {
            it.skipped = true
            return
        }
        gates.find { it.uuid == uuid }?.let {
            it.skipped = true
            return
        }
        whenGatesDouble.find { it.uuid == uuid }?.let {
            it.skipped = true
            return
        }
        whenGatesTriple.find { it.uuid == uuid }?.let {
            it.skipped = true
            return
        }
    }

    private fun RestoreRepository.RestoreCandidate.isFastTrackCapable(): Boolean {
        if(doubleTapActions.any { (it.actionRequirements?.isNotEmpty() == true || !it.supported) && !it.skipped }) return false
        if(tripleTapActions.any { (it.actionRequirements?.isNotEmpty() == true || !it.supported) && !it.skipped }) return false
        if(gates.any { (it.gateRequirements?.isNotEmpty() == true || !it.supported) && !it.skipped }) return false
        if(whenGatesDouble.any { (it.gateRequirements?.isNotEmpty() == true || !it.supported) && !it.skipped }) return false
        if(whenGatesTriple.any { (it.gateRequirements?.isNotEmpty() == true || !it.supported) && !it.skipped }) return false
        return true
    }

    private fun RestoreRepository.RestoreCandidate.areAllActionsResolved(): Boolean {
        if(doubleTapActions.any { it.actionRequirements?.isNotEmpty() == true && !it.skipped && it.supported }) return false
        if(tripleTapActions.any { it.actionRequirements?.isNotEmpty() == true && !it.skipped && it.supported }) return false
        if(gates.any { it.gateRequirements?.isNotEmpty() == true && !it.skipped && it.supported }) return false
        if(whenGatesDouble.any { it.gateRequirements?.isNotEmpty() == true && !it.skipped && it.supported }) return false
        if(whenGatesTriple.any { it.gateRequirements?.isNotEmpty() == true && !it.skipped && it.supported }) return false
        return true
    }

    private suspend fun createItems(restoreCandidate: RestoreRepository.RestoreCandidate) = withContext(Dispatchers.IO) {
        return@withContext ArrayList<Item>().apply {
            val infobox = Item.Infobox(R.string.settings_backup_restore_restore_infobox)
            if (restoreCandidate.doubleTapActions.isNotEmpty()) {
                val items = restoreCandidate.doubleTapActions.mapNotNull {
                    if(it.actionRequirements.isNullOrEmpty()) return@mapNotNull null
                    if(it.skipped) return@mapNotNull null
                    val action =
                        TapTapActionDirectory.valueFor(it.action.name ?: return@mapNotNull null)
                            ?: return@mapNotNull null
                    Item.Requirement(
                        it.uuid,
                        action.iconRes,
                        action.nameRes,
                        action.descriptionRes,
                        R.string.settings_backup_restore_restore_item_chip_action,
                        R.drawable.ic_actions,
                        false,
                        it.actionRequirements?.map { ResolvedRequirement.Action(it) } ?: emptyList(),
                        it.supported
                    )
                }
                if(items.isNotEmpty()){
                    add(Item.Header(R.string.settings_backup_restore_restore_header_actions))
                    addAll(items)
                }
            }
            if (restoreCandidate.tripleTapActions.isNotEmpty()) {
                val items = restoreCandidate.tripleTapActions.mapNotNull {
                    if(it.actionRequirements.isNullOrEmpty()) return@mapNotNull null
                    if(it.skipped) return@mapNotNull null
                    val action =
                        TapTapActionDirectory.valueFor(it.action.name ?: return@mapNotNull null)
                            ?: return@mapNotNull null
                    Item.Requirement(
                        it.uuid,
                        action.iconRes,
                        action.nameRes,
                        action.descriptionRes,
                        R.string.settings_backup_restore_restore_item_chip_action,
                        R.drawable.ic_actions,
                        false,
                        it.actionRequirements?.map { ResolvedRequirement.Action(it) } ?: emptyList(),
                        it.supported
                    )
                }
                if (isEmpty() && items.isNotEmpty()) {
                    add(Item.Header(R.string.settings_backup_restore_restore_header_actions))
                    addAll(items)
                }
            }
            if (restoreCandidate.gates.isNotEmpty()) {
                val items = restoreCandidate.gates.mapNotNull {
                    if(it.gateRequirements.isNullOrEmpty()) return@mapNotNull null
                    if(it.skipped) return@mapNotNull null
                    val gate = TapTapGateDirectory.valueFor(it.gate.name ?: return@mapNotNull null)
                        ?: return@mapNotNull null
                    Item.Requirement(
                        it.uuid,
                        gate.iconRes,
                        gate.nameRes,
                        gate.descriptionRes,
                        R.string.settings_backup_restore_restore_item_chip_gate,
                        R.drawable.ic_gates,
                        true,
                        it.gateRequirements?.map { ResolvedRequirement.Gate(it) } ?: emptyList(),
                        it.supported
                    )
                }
                if(items.isNotEmpty()) {
                    add(Item.Header(R.string.settings_backup_restore_restore_header_gates))
                    addAll(items)
                }
            }
            val whenGates = restoreCandidate.whenGatesDouble + restoreCandidate.whenGatesTriple
            if (whenGates.isNotEmpty()) {
                val items = whenGates.mapNotNull {
                    if(it.gateRequirements.isNullOrEmpty()) return@mapNotNull null
                    if(it.skipped) return@mapNotNull null
                    val gate = TapTapGateDirectory.valueFor(it.gate.name ?: return@mapNotNull null)
                        ?: return@mapNotNull null
                    Item.Requirement(
                        it.uuid,
                        gate.iconRes,
                        gate.nameRes,
                        gate.whenDescriptionRes,
                        R.string.settings_backup_restore_restore_item_chip_requirement,
                        R.drawable.ic_action_chip_when_normal,
                        true,
                        it.gateRequirements?.map { ResolvedRequirement.Gate(it) } ?: emptyList(),
                        it.supported
                    )
                }
                if(items.isNotEmpty()){
                    add(Item.Header(R.string.settings_backup_restore_restore_header_when_gates))
                    addAll(items)
                }
            }
            if (isNotEmpty()) {
                add(0, infobox)
            }
        }
    }

    override fun onCloseClicked() {
        viewModelScope.launch {
            navigation.restartActivity()
        }
    }

    override suspend fun checkRoot(): Boolean {
        return withContext(Dispatchers.IO){
            Shell_isRooted()
        }
    }

    override fun showNoRoot() {
        viewModelScope.launch {
            navigation.navigate(R.id.action_global_nav_graph_shared_no_root)
        }
    }

}