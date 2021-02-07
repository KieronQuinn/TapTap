package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapFileRepository
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dinglisch.android.tasker.TaskerIntent

class SettingsBackupRestoreRestoreViewModel(private val tapFileRepository: TapFileRepository): BaseViewModel() {

    val state = MutableStateFlow<State>(State.Running)

    fun getTitle(context: Context): Flow<String> = flow {
        state.collect {
            emit(when(it){
                is State.Running -> context.getString(R.string.settings_backuprestore_restore_title)
                is State.Cancelled -> context.getString(R.string.settings_backuprestore_restore_title)
                is State.PermissionCheck -> context.getString(R.string.settings_backuprestore_restore_title)
                is State.Write -> context.getString(R.string.settings_backuprestore_restore_title)
                is State.Done -> context.getString(R.string.settings_backuprestore_restore_done_title)
                is State.Error -> context.getString(R.string.settings_backuprestore_restore_error_title)
            })
        }
    }

    fun getContent(context: Context): Flow<String> = flow {
        state.collect {
            emit(when(it){
                is State.Running -> context.getString(R.string.settings_backuprestore_restore_desc)
                is State.Cancelled -> context.getString(R.string.settings_backuprestore_restore_desc)
                is State.PermissionCheck -> context.getString(R.string.settings_backuprestore_restore_desc)
                is State.Write -> context.getString(R.string.settings_backuprestore_restore_desc)
                is State.Done -> context.getString(R.string.settings_backuprestore_restore_done_desc, it.fileName)
                is State.Error -> context.getString(R.string.settings_backuprestore_restore_error_desc, context.getString(it.errorType.errorRes))
            })
        }
    }

    fun startRestore(context: Context, fileUri: Uri) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val file = DocumentFile.fromSingleUri(context, fileUri) ?: run {
                state.emit(State.Error(ErrorType.ERROR_GETTING_FILE))
                return@withContext
            }
            runCatching {
                val stream = context.contentResolver.openInputStream(file.uri) ?: run {
                    state.emit(State.Error(ErrorType.ERROR_READING_FILE))
                    return@withContext
                }
                var backupJson: TapFileRepository.BackupJson? = null
                stream.run {
                    backupJson = tapFileRepository.loadBackupJson(file.name!!, this.readBytes().ungzip())
                    close()
                }
                //Artificial delay to give the indication that it's actually *done* something
                delay(2000L)
                backupJson
            }.onFailure {
                state.emit(State.Error(ErrorType.UNKNOWN))
            }.onSuccess { backup ->
                if (backup != null) {
                    val actionGates = backup.doubleTapActions.map { ActionGate.Action(it) }
                        .plus(backup.tripleTapActions.map { ActionGate.Action(it) }
                            .plus(backup.gates.map { ActionGate.Gate(it) }))
                        .plus(
                            backup.getWhenGates().map { ActionGate.WhenGate(it.first, it.second) })
                    state.emit(State.PermissionCheck(backup, actionGates, emptyList()))
                } else {
                    state.emit(State.Error(ErrorType.ERROR_READING_FILE))
                }
            }
        }
    }

    fun handlePermissionCheck(fragment: Fragment, backupJson: TapFileRepository.BackupJson, toCheck: List<ActionGate>, skipped: List<SkippedItem>) = viewModelScope.launch {
        if(toCheck.isEmpty()){
            state.emit(State.Write(backupJson, skipped))
        }else{
            val actionOrGate = toCheck.first()
            if(!actionOrGate.isSupported(fragment.requireContext())) {
                skipAndMoveOn(SkippedReason.NOT_AVAILABLE)
                return@launch
            }
            when(actionOrGate){
                is ActionGate.Action -> {
                    val requirement = actionOrGate.action.getActionDataRequirement(fragment.requireContext())
                    if(requirement == null){
                        //Just move on
                        iterateAndMoveOn()
                    }else{
                        if(!showPrompt(fragment, requirement, actionOrGate)){
                            skipAndMoveOn(requirement.getDefaultSkipReason())
                        }
                    }
                }
                is ActionGate.Gate -> {
                    val requirement = actionOrGate.gate.getGateDataRequirement(fragment.requireContext())
                    if(requirement == null){
                        //Just move on
                        iterateAndMoveOn()
                    }else if(!showPrompt(fragment, requirement, actionOrGate)){
                        skipAndMoveOn(requirement.getDefaultSkipReason())
                    }
                }
                is ActionGate.WhenGate -> {
                    val requirement = actionOrGate.gate.toGateInternal().getGateDataRequirement(fragment.requireContext())
                    if(requirement == null){
                        //Just move on
                        iterateAndMoveOn()
                    }else{
                        if(!showPrompt(fragment, requirement, actionOrGate)){
                            skipAndMoveOn(requirement.getDefaultSkipReason())
                        }
                    }
                }
            }
        }
    }

    private fun ActionGate.isSupported(context: Context): Boolean {
        return when(this){
            is ActionGate.Action -> action.action.isSupported(context)
            is ActionGate.Gate -> gate.gate.isSupported(context)
            is ActionGate.WhenGate -> gate.gate.isSupported(context)
        }
    }

    private fun ActionDataRequirement.getDefaultSkipReason(): SkippedReason {
        return when(this){
            is ActionDataRequirement.Tasker -> SkippedReason.TASKER_NOT_INSTALLED
            is ActionDataRequirement.AppInstalled -> SkippedReason.APP_NOT_INSTALLED
            else -> SkippedReason.USER_SKIPPED
        }
    }

    private fun showPrompt(fragment: Fragment, requirement: ActionDataRequirement, actionOrGate: ActionGate): Boolean = when(requirement) {
        is ActionDataRequirement.Permission -> {
            fragment.navigate(SettingsBackupRestoreRestoreFragmentDirections.actionSettingsBackupRestoreRestoreFragmentToSettingsRestorePermissionBottomSheetFragment(requirement, actionOrGate))
            true
        }
        is ActionDataRequirement.NotificationPermission -> {
            fragment.navigate(SettingsBackupRestoreRestoreFragmentDirections.actionSettingsBackupRestoreRestoreFragmentToSettingsRestoreNotificationPermissionBottomSheetFragment(actionOrGate))
            true
        }
        is ActionDataRequirement.GestureService -> {
            fragment.navigate(SettingsBackupRestoreRestoreFragmentDirections.actionSettingsBackupRestoreRestoreFragmentToSettingsRestoreGestureServiceBottomSheetFragment(actionOrGate))
            true
        }
        is ActionDataRequirement.Tasker -> {
            if(fragment.requireContext().isTaskerInstalled()){
                //Permission needs granting
                fragment.navigate(SettingsBackupRestoreRestoreFragmentDirections.actionSettingsBackupRestoreRestoreFragmentToSettingsRestoreTaskerPermissionBottomSheetFragment(actionOrGate))
                true
            }else{
                false
            }
        }
        else -> false
    }

    fun showSkippedDialog(fragment: Fragment, skipped: List<SkippedItem>){
        fragment.navigate(SettingsBackupRestoreRestoreFragmentDirections.actionSettingsBackupRestoreRestoreFragmentToSettingsBackupRestoreSkippedBottomSheetFragment(skipped.toTypedArray()))
    }

    suspend fun iterateAndMoveOn(){
        (state.value as? State.PermissionCheck)?.let {
            state.emit(State.PermissionCheck(it.backupJson, it.toCheck.minus(it.toCheck.first()), it.skipped))
        }
    }

    suspend fun skipAndMoveOn(reason: SkippedReason){
        (state.value as? State.PermissionCheck)?.let {
            val skipItem = it.toCheck.first()
            state.emit(State.PermissionCheck(it.backupJson, it.toCheck.subList(1, it.toCheck.size), it.skipped.plus(SkippedItem(skipItem, reason))))
        }
    }

    fun handleWrite(backupJson: TapFileRepository.BackupJson, skipped: List<SkippedItem>) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val skippedActions =
                skipped.mapNotNull { if (it.actionOrGate is ActionGate.Action) it.actionOrGate.action else null }
            val skippedGates = skipped.mapNotNull {
                when (it.actionOrGate) {
                    is ActionGate.Gate -> it.actionOrGate.gate
                    is ActionGate.WhenGate -> it.actionOrGate.gate.toGateInternal()
                    else -> null
                }
            }
            if (tapFileRepository.restoreBackupJson(
                    backupJson,
                    skippedActions.toTypedArray(),
                    skippedGates.toTypedArray()
                )
            ) {
                state.emit(State.Done(backupJson.fileName, skipped))
            } else {
                state.emit(State.Error(ErrorType.ERROR_READING_FILE))
            }
        }
    }

    fun onCloseClicked(fragment: Fragment){
        fragment.findNavController().navigateUp()
    }

    sealed class State {
        object Running: State()
        data class PermissionCheck(val backupJson: TapFileRepository.BackupJson, val toCheck: List<ActionGate>, val skipped: List<SkippedItem>): State()
        data class Write(val backupJson: TapFileRepository.BackupJson, val skipped: List<SkippedItem>): State()
        data class Done(val fileName: String, val skipped: List<SkippedItem>): State()
        data class Error(val errorType: ErrorType): State()
        object Cancelled: State()
    }

    enum class ErrorType(@StringRes val errorRes: Int) {
        ERROR_GETTING_FILE(R.string.backup_restore_error_type_failed_getting_file),
        ERROR_READING_FILE(R.string.backup_restore_error_type_failed_read_file),
        UNKNOWN(R.string.backup_restore_error_type_failed_unknown)
    }

    sealed class ActionGate: Parcelable {
        @Parcelize
        data class Action(val action: ActionInternal): ActionGate()
        @Parcelize
        data class Gate(val gate: GateInternal): ActionGate()
        @Parcelize
        data class WhenGate(val gate: WhenGateInternal, val parentAction: ActionInternal): ActionGate()
    }

    private fun ActionInternal.getActionDataRequirement(context: Context): ActionDataRequirement? {
        if(action.isActionDataSatisfied(context)) return null
        return when(action.dataType){
            ActionDataTypes.ACCESS_NOTIFICATION_POLICY -> ActionDataRequirement.NotificationPermission
            ActionDataTypes.SHORTCUT -> {
                val intent = Intent().deserialize(data!!)
                if(intent.doesExist(context)) {
                    null
                }else{
                    ActionDataRequirement.AppInstalled(intent.`package` ?: "Unknown")
                }
            }
            ActionDataTypes.PACKAGE_NAME -> {
                if(context.isAppLaunchable(data!!)){
                    null
                }else {
                    ActionDataRequirement.AppInstalled(data!!)
                }
            }
            ActionDataTypes.SECONDARY_GESTURE_SERVICE -> ActionDataRequirement.GestureService
            ActionDataTypes.TASKER_TASK -> {
                if(context.isTaskerInstalled() && TaskerIntent.testStatus(context) == TaskerIntent.Status.OK) null
                else ActionDataRequirement.Tasker
            }
            ActionDataTypes.CAMERA_PERMISSION -> ActionDataRequirement.Permission(listOf(Manifest.permission.CAMERA))
            ActionDataTypes.ANSWER_PHONE_CALLS_PERMISSION -> ActionDataRequirement.Permission(listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS))
            else -> null
        }
    }

    private fun GateInternal.getGateDataRequirement(context: Context): ActionDataRequirement? {
        if(gate.isGateDataSatisfied(context)) return null
        return when(gate.dataType){
            GateDataTypes.PACKAGE_NAME -> {
                if(context.isAppLaunchable(data!!)){
                    null
                }else {
                    ActionDataRequirement.AppInstalled(data!!)
                }
            }
            else -> null
        }
    }

    fun reset() = viewModelScope.launch {
        state.emit(State.Running)
    }

    fun cancel() = viewModelScope.launch {
        state.emit(State.Cancelled)
    }

    sealed class ActionDataRequirement: Parcelable {
        @Parcelize
        object Tasker: ActionDataRequirement()
        @Parcelize
        object NotificationPermission: ActionDataRequirement()
        @Parcelize
        data class Permission(val permission: List<String>): ActionDataRequirement()
        @Parcelize
        data class AppInstalled(val packageName: String): ActionDataRequirement()
        @Parcelize
        object GestureService: ActionDataRequirement()
    }

    enum class SkippedReason(val descriptionRes: Int) {
        USER_SKIPPED(R.string.settings_backuprestore_restore_skipped_reason_user_skipped),
        APP_NOT_INSTALLED(R.string.settings_backuprestore_restore_skipped_reason_app_not_installed),
        TASKER_NOT_INSTALLED(R.string.settings_backuprestore_restore_skipped_reason_tasker),
        NOT_AVAILABLE(R.string.settings_backuprestore_restore_skipped_reason_not_available)
    }

    @Parcelize
    data class SkippedItem(val actionOrGate: ActionGate, val reason: SkippedReason): Parcelable

}