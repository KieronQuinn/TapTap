package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.extensions.getShortcutAsData
import com.kieronquinn.app.taptap.utils.extensions.navigate
import com.kieronquinn.app.taptap.utils.extensions.observeOneShot
import com.kieronquinn.app.taptap.ui.activities.AppPickerActivity
import com.kieronquinn.app.taptap.ui.screens.picker.app.AppPickerFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import kotlinx.coroutines.launch
import net.dinglisch.android.tasker.TaskerIntent

class SettingsActionAddListViewModel: ViewModel() {

    fun getActions(context: Context, tapActionCategory: TapActionCategory): Array<TapAction> {
        return TapAction.values().filter { it.category == tapActionCategory && it.isSupported(context) }.toTypedArray()
    }

    fun onActionClicked(fragment: SettingsActionAddListFragment, sharedViewModel: SettingsActionAddContainerBottomSheetViewModel, action: TapAction) = viewModelScope.launch {
        if(action.isActionDataSatisfied(fragment.requireContext())){
            sharedViewModel.addAction(action)
        }else{
            when(val dataTypeAction = action.getDataTypeAction(fragment, sharedViewModel)){
                is DataTypeAction.ActivityResult -> {
                    fragment.activityResultLiveData.observeOneShot(fragment.viewLifecycleOwner) {
                        dataTypeAction.callback.invoke(it)
                    }
                    fragment.activityLauncher.launch(dataTypeAction.intent)
                }
                is DataTypeAction.Permission -> {
                    fragment.permissionResultLiveData.observeOneShot(fragment.viewLifecycleOwner) {
                        dataTypeAction.callback.invoke(it)
                    }
                    fragment.permissionLauncher.launch(dataTypeAction.requestedPermission.toTypedArray())
                }
                is DataTypeAction.TaskerTask -> {
                    if(checkTaskerPermission(fragment.requireContext())){
                        fragment.activityResultLiveData.observeOneShot(fragment.viewLifecycleOwner) {
                            dataTypeAction.callback.invoke(it)
                        }
                        fragment.activityLauncher.launch(TaskerIntent.getTaskSelectIntent())
                    }else{
                        fragment.navigate(SettingsActionAddListFragmentDirections.actionActionListFragmentToSettingsActionAddTaskerBottomSheetFragment())
                    }
                }
                is DataTypeAction.GestureService -> {
                    fragment.navigate(SettingsActionAddListFragmentDirections.actionActionListFragmentToSettingsActionAddGestureServiceBottomSheetFragment(action))
                }
                is DataTypeAction.NotificationPermission -> {
                    fragment.navigate(SettingsActionAddListFragmentDirections.actionActionListFragmentToSettingsActionAddNotificationPolicyBottomSheetFragment(action))
                }
                else -> {
                    sharedViewModel.addAction(action)
                }
            }
        }
    }

    private fun TapAction.getDataTypeAction(fragment: SettingsActionAddListFragment, sharedViewModel: SettingsActionAddContainerBottomSheetViewModel): DataTypeAction<*>? {
        val context = fragment.requireContext()
        return when(dataType){
            ActionDataTypes.PACKAGE_NAME -> {
                DataTypeAction.ActivityResult(Intent(context, AppPickerActivity::class.java)){
                    if(it?.resultCode == Activity.RESULT_OK){
                        val selectedApp = it.data?.getStringExtra(AppPickerFragment.KEY_APP) ?: return@ActivityResult
                        sharedViewModel.addAction(this, selectedApp)
                    }
                }
            }
            ActionDataTypes.CAMERA_PERMISSION -> {
                DataTypeAction.Permission(listOf(Manifest.permission.CAMERA)){
                    if(isActionDataSatisfied(context)){
                        sharedViewModel.addAction(this)
                    }
                }
            }
            ActionDataTypes.ANSWER_PHONE_CALLS_PERMISSION -> {
                DataTypeAction.Permission(listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS)){
                    if(isActionDataSatisfied(context)){
                        sharedViewModel.addAction(this)
                    }
                }
            }
            ActionDataTypes.TASKER_TASK -> {
                DataTypeAction.TaskerTask {
                    if(it?.resultCode == Activity.RESULT_OK){
                        val selectedTask = it.data?.dataString ?: return@TaskerTask
                        sharedViewModel.addAction(this, selectedTask)
                    }
                }
            }
            ActionDataTypes.SHORTCUT -> {
                DataTypeAction.ActivityResult(Intent(Intent.ACTION_PICK_ACTIVITY).apply {
                    putExtra(Intent.EXTRA_INTENT, Intent(Intent.ACTION_CREATE_SHORTCUT))
                }){
                    if(it?.resultCode == Activity.RESULT_OK && it.data != null){
                        handleShortcutIntent(fragment, it.data!!, sharedViewModel)
                    }
                }
            }
            ActionDataTypes.SECONDARY_GESTURE_SERVICE -> {
                DataTypeAction.GestureService {
                    if(isActionDataSatisfied(context)){
                        sharedViewModel.addAction(this)
                    }
                }
            }
            ActionDataTypes.ACCESS_NOTIFICATION_POLICY -> {
                DataTypeAction.NotificationPermission {
                    if(isActionDataSatisfied(context)){
                        sharedViewModel.addAction(this)
                    }
                }
            }
            else -> null
        }
    }

    private fun handleShortcutIntent(fragment: SettingsActionAddListFragment, intent: Intent, sharedViewModel: SettingsActionAddContainerBottomSheetViewModel){
        if(intent.component != null){
            //Handle the secondary intent for configuration
            fragment.activityResultLiveData.observeOneShot(fragment.viewLifecycleOwner) {
                if(it?.resultCode == Activity.RESULT_OK && it.data != null) {
                    addIntentAsAction(it.data!!, fragment, sharedViewModel)
                }
            }
            fragment.activityLauncher.launch(intent)
        }else{
            addIntentAsAction(intent, fragment, sharedViewModel)
        }
    }

    private fun addIntentAsAction(intent: Intent, fragment: Fragment, sharedViewModel: SettingsActionAddContainerBottomSheetViewModel){
        //Add the action
        intent.getShortcutAsData()?.let {
            sharedViewModel.addAction(TapAction.LAUNCH_SHORTCUT, it)
        } ?: run {
            Toast.makeText(
                fragment.requireContext(),
                fragment.getString(R.string.action_launch_shortcut_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkTaskerPermission(context: Context): Boolean {
        return TaskerIntent.testStatus(context) == TaskerIntent.Status.OK
    }

    private sealed class DataTypeAction<T>(open val callback: (T) -> Unit) {
        data class ActivityResult(val intent: Intent, override val callback: (androidx.activity.result.ActivityResult?) -> Unit): DataTypeAction<androidx.activity.result.ActivityResult?>(callback)
        data class TaskerTask(override val callback: (androidx.activity.result.ActivityResult?) -> Unit): DataTypeAction<androidx.activity.result.ActivityResult?>(callback)
        data class Permission(val requestedPermission: List<String>, override val callback: (Boolean?) -> Unit): DataTypeAction<Boolean?>(callback)
        data class NotificationPermission(override val callback: (androidx.activity.result.ActivityResult?) -> Unit): DataTypeAction<androidx.activity.result.ActivityResult?>(callback)
        data class GestureService(override val callback: (androidx.activity.result.ActivityResult?) -> Unit): DataTypeAction<androidx.activity.result.ActivityResult?>(callback)
    }

}