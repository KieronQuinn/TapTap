package com.kieronquinn.app.taptap.repositories.actions

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.google.gson.Gson
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.accessibility.TapTapAccessibilityRouter.AccessibilityInput.PerformSwipe.Direction
import com.kieronquinn.app.taptap.components.columbus.actions.TapTapAction
import com.kieronquinn.app.taptap.components.columbus.actions.custom.*
import com.kieronquinn.app.taptap.models.action.ActionDataTypes
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.ActionSupportedRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory.*
import com.kieronquinn.app.taptap.models.columbus.AppShortcutData
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabase
import com.kieronquinn.app.taptap.repositories.room.actions.Action
import com.kieronquinn.app.taptap.repositories.room.actions.DoubleTapAction
import com.kieronquinn.app.taptap.repositories.room.actions.TripleTapAction
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryDouble
import com.kieronquinn.app.taptap.repositories.whengates.WhenGatesRepositoryTriple
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.service.accessibility.TapTapGestureAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.app.taptap.utils.flow.FlowQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ActionsRepository {

    abstract val onChanged: Flow<Unit>

    abstract suspend fun populateDefaultActions(context: Context)

    abstract suspend fun createTapTapAction(
        action: Action,
        isTriple: Boolean,
        context: Context,
        serviceLifecycle: Lifecycle,
        serviceRepository: TapTapShizukuServiceRepository,
        rootServiceRepository: TapTapRootServiceRepository
    ): TapTapAction?

    abstract suspend fun getNextDoubleTapActionIndex(): Int
    abstract suspend fun getNextTripleTapActionIndex(): Int

    abstract suspend fun getSavedDoubleTapActions(): List<DoubleTapAction>
    abstract suspend fun getSavedTripleTapActions(): List<TripleTapAction>

    abstract suspend fun clearDoubleTapActions()
    abstract suspend fun addDoubleTapAction(doubleTapAction: DoubleTapAction): Long
    abstract suspend fun addTripleTapAction(tripleTapAction: TripleTapAction): Long

    abstract suspend fun clearTripleTapActions()
    abstract suspend fun moveDoubleTapAction(fromIndex: Int, toIndex: Int)
    abstract suspend fun moveTripleTapAction(fromIndex: Int, toIndex: Int)
    abstract suspend fun deleteDoubleTapAction(id: Int)
    abstract suspend fun deleteTripleTapAction(id: Int)

    abstract suspend fun isActionRequirementSatisfied(
        context: Context,
        actionDirectory: TapTapActionDirectory
    ): Boolean

    abstract fun isActionSupported(
        context: Context,
        actionDirectory: TapTapActionDirectory
    ): Boolean

    abstract fun getUnsupportedReason(
        context: Context,
        actionDirectory: TapTapActionDirectory
    ): ActionSupportedRequirement?

    abstract fun isActionDataSatisfied(
        context: Context,
        data: ActionDataTypes,
        extraData: String
    ): Boolean

    abstract fun getFormattedDescriptionForAction(
        context: Context,
        actionDirectory: TapTapActionDirectory,
        extraData: String?
    ): CharSequence

}

class ActionsRepositoryImpl(
    database: TapTapDatabase,
    private val whenGatesRepositoryDouble: WhenGatesRepositoryDouble<*>,
    private val whenGatesRepositoryTriple: WhenGatesRepositoryTriple<*>,
    private val gson: Gson
) : ActionsRepository() {

    private val actionsDao = database.actionsDao()

    private val savedDoubleTapActions = actionsDao.getAllDoubleTapAsFlow().flowOn(Dispatchers.IO)
        .stateIn(GlobalScope, SharingStarted.Lazily, null)

    private val savedTripleTapActions = actionsDao.getAllTripleTapAsFlow().flowOn(Dispatchers.IO)
        .stateIn(GlobalScope, SharingStarted.Lazily, null)

    override val onChanged = MutableSharedFlow<Unit>()

    override suspend fun populateDefaultActions(context: Context) {
        val defaultDoubleTapActions = if(isActionSupported(context, SCREENSHOT)){
            arrayOf(LAUNCH_ASSISTANT, SCREENSHOT)
        }else{
            arrayOf(LAUNCH_ASSISTANT, HOME)
        }.mapIndexed { index, tapTapActionDirectory ->
            DoubleTapAction(
                name = tapTapActionDirectory.name,
                index = index,
                extraData = ""
            )
        }
        val defaultTripleTapActions = arrayOf(NOTIFICATIONS)
            .mapIndexed { index, tapTapActionDirectory ->
                TripleTapAction(
                    name = tapTapActionDirectory.name,
                    index = index,
                    extraData = ""
                )
            }
        actionsQueue.add(ActionsAction.GenericAction {
            defaultDoubleTapActions.forEach {
                actionsDao.insert(it)
            }
            defaultTripleTapActions.forEach {
                actionsDao.insert(it)
            }
        })
    }

    override suspend fun getNextDoubleTapActionIndex(): Int = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.ActionsDoubleListRequiringAction { actions ->
                it.resume((actions.maxOfOrNull { it.index } ?: -1) + 1)
            })
        }
    }

    override suspend fun getNextTripleTapActionIndex(): Int = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.ActionsTripleListRequiringAction { actions ->
                it.resume((actions.maxOfOrNull { it.index } ?: -1) + 1)
            })
        }
    }

    override suspend fun getSavedDoubleTapActions()
        = savedDoubleTapActions.filterNotNull().first()

    override suspend fun getSavedTripleTapActions()
        = savedTripleTapActions.filterNotNull().first()

    sealed class ActionsAction {
        abstract class ActionsListRequiringAction<T: Action>(open val block: suspend (actions: List<T>) -> Unit): ActionsAction()
        data class ActionsDoubleListRequiringAction(override val block: suspend (actions: List<DoubleTapAction>) -> Unit): ActionsListRequiringAction<DoubleTapAction>(block)
        data class ActionsTripleListRequiringAction(override val block: suspend (actions: List<TripleTapAction>) -> Unit): ActionsListRequiringAction<TripleTapAction>(block)
        data class GenericAction(val block: suspend () -> Unit): ActionsAction()
    }

    private val actionsQueue = FlowQueue<ActionsAction>()

    private fun setupActionsQueue() = GlobalScope.launch {
        actionsQueue.asFlow().debounce(500L).collect {
            while (true) {
                val item = actionsQueue.asQueue().removeFirstOrNull() ?: break
                when(item){
                    is ActionsAction.ActionsDoubleListRequiringAction -> {
                        item.block.invoke(getSavedDoubleTapActions())
                    }
                    is ActionsAction.ActionsTripleListRequiringAction -> {
                        item.block.invoke(getSavedTripleTapActions())
                    }
                    is ActionsAction.GenericAction -> {
                        item.block()
                    }
                    else -> throw NotImplementedError("ActionsAction ${item.javaClass.simpleName} not implemented")
                }
            }
            onChanged.emit(Unit)
        }
    }

    init {
        setupActionsQueue()
    }

    override suspend fun clearDoubleTapActions() {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.GenericAction {
                actionsDao.deleteAllDoubleTap()
            })
        }
    }

    override suspend fun addDoubleTapAction(doubleTapAction: DoubleTapAction): Long = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.GenericAction {
                it.resume(actionsDao.insert(doubleTapAction))
            })
        }
    }

    override suspend fun clearTripleTapActions() {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.GenericAction {
                actionsDao.deleteAllTripleTap()
            })
        }
    }

    override suspend fun addTripleTapAction(tripleTapAction: TripleTapAction): Long = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(ActionsAction.GenericAction {
                it.resume(actionsDao.insert(tripleTapAction))
            })
        }
    }

    override suspend fun deleteDoubleTapAction(id: Int) {
        actionsQueue.add(ActionsAction.ActionsDoubleListRequiringAction { actions ->
            val action = actions.firstOrNull { it.actionId == id } ?: return@ActionsDoubleListRequiringAction
            actionsDao.delete(action)
        })
        whenGatesRepositoryDouble.removeAllWhenGates(id)
    }

    override suspend fun deleteTripleTapAction(id: Int) {
        actionsQueue.add(ActionsAction.ActionsTripleListRequiringAction { actions ->
            val action = actions.firstOrNull { it.actionId == id } ?: return@ActionsTripleListRequiringAction
            actionsDao.delete(action)
        })
        whenGatesRepositoryTriple.removeAllWhenGates(id)
    }

    override suspend fun moveDoubleTapAction(fromIndex: Int, toIndex: Int) {
        actionsQueue.add(ActionsAction.ActionsDoubleListRequiringAction { actions ->
            if (fromIndex < toIndex) {
                for (i in fromIndex until toIndex) {
                    Collections.swap(actions, i, i + 1)
                }
            } else {
                for (i in fromIndex downTo toIndex + 1) {
                    Collections.swap(actions, i, i - 1)
                }
            }
            actions.forEachIndexed { index, action ->
                action.index = index
                actionsDao.update(action)
            }
        })
    }

    override suspend fun moveTripleTapAction(fromIndex: Int, toIndex: Int) {
        actionsQueue.add(ActionsAction.ActionsTripleListRequiringAction { actions ->
            if (fromIndex < toIndex) {
                for (i in fromIndex until toIndex) {
                    Collections.swap(actions, i, i + 1)
                }
            } else {
                for (i in fromIndex downTo toIndex + 1) {
                    Collections.swap(actions, i, i - 1)
                }
            }
            actions.forEachIndexed { index, action ->
                action.index = index
                actionsDao.update(action)
            }
        })
    }

    @SuppressLint("InlinedApi")
    override fun isActionDataSatisfied(
        context: Context,
        data: ActionDataTypes,
        extraData: String
    ): Boolean {
        return when (data) {
            //Those that require a picker are satisfied if the extra data is included
            ActionDataTypes.PACKAGE_NAME, ActionDataTypes.APP_SHORTCUT, ActionDataTypes.SHORTCUT, ActionDataTypes.TASKER_TASK, ActionDataTypes.QUICK_SETTING -> extraData.isNotBlank()
        }
    }

    override fun getUnsupportedReason(
        context: Context,
        actionDirectory: TapTapActionDirectory
    ): ActionSupportedRequirement? {
        return when (actionDirectory.actionSupportedRequirement) {
            is ActionSupportedRequirement.MinSdk -> {
                return if(Build.VERSION.SDK_INT < actionDirectory.actionSupportedRequirement.version){
                    actionDirectory.actionSupportedRequirement
                }else null
            }
            is ActionSupportedRequirement.Intent -> {
                return if(context.packageManager.resolveActivity(actionDirectory.actionSupportedRequirement.intent, 0) == null){
                    actionDirectory.actionSupportedRequirement
                }else null
            }
            is ActionSupportedRequirement.Tasker -> {
                return if(!context.isTaskerInstalled()){
                    actionDirectory.actionSupportedRequirement
                }else null
            }
            is ActionSupportedRequirement.Snapchat -> {
                return if(!context.isPackageInstalled(SnapchatAction.PACKAGE_NAME)){
                    actionDirectory.actionSupportedRequirement
                }else null
            }
            else -> null
        }
    }

    override fun isActionSupported(
        context: Context,
        actionDirectory: TapTapActionDirectory
    ): Boolean {
        return getUnsupportedReason(context, actionDirectory) == null
    }

    override fun getFormattedDescriptionForAction(
        context: Context,
        actionDirectory: TapTapActionDirectory,
        extraData: String?
    ): CharSequence {
        if (extraData?.isNotBlank() != true || actionDirectory.formattableDescription == null) {
            return context.getString(actionDirectory.descriptionRes)
        }
        val formattedText = when (actionDirectory.dataType) {
            ActionDataTypes.PACKAGE_NAME -> context.packageManager.getApplicationLabel(extraData)
                ?: context.getString(R.string.item_action_app_uninstalled, extraData)
            ActionDataTypes.SHORTCUT -> getPackageNameForShortcut(context, extraData)
            ActionDataTypes.APP_SHORTCUT -> context.packageManager.getApplicationLabel(
                gson.fromJson(
                    extraData,
                    AppShortcutData::class.java
                ).packageName
            )
            ActionDataTypes.TASKER_TASK -> extraData
            ActionDataTypes.QUICK_SETTING -> {
                val component = ComponentName.unflattenFromString(extraData)
                context.packageManager.getServiceLabel(component) ?: context.packageManager.getApplicationLabel(component?.packageName)
            }
            else -> null
        } ?: run {
            return context.getString(actionDirectory.descriptionRes)
        }
        return context.getString(actionDirectory.formattableDescription, formattedText)
    }

    private fun getPackageNameForShortcut(context: Context, data: String): CharSequence? {
        val intent = Intent().apply {
            deserialize(data)
        }
        return try {
            context.packageManager.queryIntentActivities(intent, 0).firstOrNull()?.let {
                context.packageManager.getApplicationLabel(it.activityInfo.packageName)
                    ?: context.getString(
                        R.string.item_action_app_uninstalled,
                        it.activityInfo.packageName
                    )
            } ?: run {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createTapTapAction(
        action: Action,
        isTriple: Boolean,
        context: Context,
        serviceLifecycle: Lifecycle,
        serviceRepository: TapTapShizukuServiceRepository,
        rootServiceRepository: TapTapRootServiceRepository
    ): TapTapAction? {
        val actionName = action.name
        val tapAction =
            TapTapActionDirectory.values().firstOrNull { it.name == actionName } ?: return null
        val whenGatesRepository = if(isTriple) whenGatesRepositoryTriple  else whenGatesRepositoryDouble
        val whenGates = withContext(Dispatchers.IO) {
            whenGatesRepository.getSavedWhenGates(action.actionId)
        }.mapNotNull { whenGatesRepository.createTapTapWhenGate(it, context, serviceLifecycle) }
        return when (tapAction) {
            LAUNCH_APP -> LaunchAppAction(
                serviceLifecycle,
                context,
                action.extraData,
                whenGates,
                emptySet()
            )
            LAUNCH_SHORTCUT -> LaunchShortcutAction(
                serviceLifecycle,
                context,
                action.extraData,
                whenGates,
                emptySet()
            )
            LAUNCH_APP_SHORTCUT -> LaunchAppShortcutAction(
                serviceLifecycle,
                context,
                action.extraData,
                whenGates,
                emptySet(),
                serviceRepository
            )
            LAUNCH_ASSISTANT -> LaunchAssistantAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet()
            )
            LAUNCH_SEARCH -> LaunchSearchAction(serviceLifecycle, context, whenGates, emptySet())
            LAUNCH_CAMERA -> LaunchCameraAction(serviceLifecycle, context, whenGates, emptySet())
            SNAPCHAT -> SnapchatAction(serviceLifecycle, context, whenGates, emptySet())
            SCREENSHOT -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT,
                whenGates,
                emptySet()
            )
            NOTIFICATIONS -> NotificationsExpandAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet()
            )
            QUICK_SETTINGS -> QuickSettingsExpandAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet()
            )
            LOCK_SCREEN -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN,
                whenGates,
                emptySet()
            )
            WAKE_DEVICE -> WakeDeviceAction(serviceLifecycle, context, whenGates, emptySet())
            HOME -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_HOME,
                whenGates,
                emptySet()
            )
            BACK -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_BACK,
                whenGates,
                emptySet()
            )
            RECENTS -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_RECENTS,
                whenGates,
                emptySet()
            )
            SPLIT_SCREEN -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN,
                whenGates,
                emptySet()
            )
            REACHABILITY -> LaunchReachabilityAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet()
            )
            POWER_DIALOG -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_POWER_DIALOG,
                whenGates,
                emptySet()
            )
            APP_DRAWER -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS,
                whenGates,
                emptySet()
            )
            ALT_TAB -> AltTabAction(serviceLifecycle, context, whenGates, emptySet())
            FLASHLIGHT -> FlashlightAction(serviceLifecycle, context, whenGates, emptySet())
            TASKER_EVENT -> TaskerEventAction(serviceLifecycle, context, whenGates, emptySet())
            TASKER_TASK -> TaskerTaskAction(
                serviceLifecycle,
                context,
                action.extraData,
                whenGates,
                emptySet()
            )
            TOGGLE_PAUSE -> MusicAction(
                serviceLifecycle,
                context,
                MusicAction.Command.TOGGLE_PAUSE,
                whenGates,
                emptySet()
            )
            PREVIOUS -> MusicAction(
                serviceLifecycle,
                context,
                MusicAction.Command.PREVIOUS,
                whenGates,
                emptySet()
            )
            NEXT -> MusicAction(
                serviceLifecycle,
                context,
                MusicAction.Command.NEXT,
                whenGates,
                emptySet()
            )
            SOUND_PROFILE -> SoundProfileAction(serviceLifecycle, context, whenGates, emptySet())
            DO_NOT_DISTURB -> DoNotDisturbAction(serviceLifecycle, context, whenGates, emptySet())
            VOLUME_PANEL -> VolumeAction(
                serviceLifecycle,
                context,
                AudioManager.ADJUST_SAME,
                whenGates,
                emptySet()
            )
            VOLUME_UP -> VolumeAction(
                serviceLifecycle,
                context,
                AudioManager.ADJUST_RAISE,
                whenGates,
                emptySet()
            )
            VOLUME_DOWN -> VolumeAction(
                serviceLifecycle,
                context,
                AudioManager.ADJUST_LOWER,
                whenGates,
                emptySet()
            )
            VOLUME_TOGGLE_MUTE -> VolumeAction(
                serviceLifecycle,
                context,
                AudioManager.ADJUST_TOGGLE_MUTE,
                whenGates,
                emptySet()
            )
            ALARM_TIMER -> AlarmTimerAction(serviceLifecycle, context, whenGates, emptySet())
            ALARM_SNOOZE -> AlarmSnoozeAction(serviceLifecycle, context, whenGates, emptySet())
            GOOGLE_VOICE_ACCESS -> GoogleVoiceAccessAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet()
            )
            ACCESSIBILITY_BUTTON -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_BUTTON,
                whenGates,
                emptySet()
            )
            ACCESSIBILITY_BUTTON_CHOOSER -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER,
                whenGates,
                emptySet()
            )
            ACCESSIBILITY_SHORTCUT -> AccessibilityServiceGlobalAction(
                serviceLifecycle,
                context,
                AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT,
                whenGates,
                emptySet()
            )
            HAMBURGER -> HamburgerAction(serviceLifecycle, context, whenGates, emptySet())
            ACCEPT_CALL -> AcceptCallAction(serviceLifecycle, context, whenGates, emptySet())
            REJECT_CALL -> RejectCallAction(serviceLifecycle, context, whenGates, emptySet())
            SWIPE_UP -> SwipeAction(serviceLifecycle, context, Direction.UP, whenGates, emptySet())
            SWIPE_DOWN -> SwipeAction(
                serviceLifecycle,
                context,
                Direction.DOWN,
                whenGates,
                emptySet()
            )
            SWIPE_LEFT -> SwipeAction(
                serviceLifecycle,
                context,
                Direction.LEFT,
                whenGates,
                emptySet()
            )
            SWIPE_RIGHT -> SwipeAction(
                serviceLifecycle,
                context,
                Direction.RIGHT,
                whenGates,
                emptySet()
            )
            CAMERA_SHUTTER -> CameraShutterAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet(),
                serviceRepository
            )
            DEVICE_CONTROLS -> LaunchDeviceControlsAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet(),
                rootServiceRepository
            )
            QUICK_ACCESS_WALLET -> LaunchQuickAccessWalletAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet(),
                rootServiceRepository
            )
            QUICK_SETTING -> ClickQuickSettingAction(
                serviceLifecycle,
                context,
                whenGates,
                emptySet(),
                serviceRepository,
                action.extraData
            )
        }
    }

    override suspend fun isActionRequirementSatisfied(context: Context, actionDirectory: TapTapActionDirectory): Boolean {
        return actionDirectory.actionRequirement?.all {
            when(it){
                is ActionRequirement.DrawOverOtherAppsPermission -> Settings.canDrawOverlays(context)
                is ActionRequirement.CameraPermission -> context.doesHavePermissions(Manifest.permission.CAMERA)
                is ActionRequirement.AnswerPhoneCallsPermission -> context.doesHavePermissions(Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_STATE)
                is ActionRequirement.AccessNotificationPolicyPermission -> context.doesHaveNotificationPolicyAccess()
                is ActionRequirement.TaskerPermission -> context.doesHaveTaskerPermission()
                is ActionRequirement.Accessibility -> context.isServiceRunning(TapTapAccessibilityService::class.java)
                is ActionRequirement.GestureAccessibility -> context.isServiceRunning(TapTapGestureAccessibilityService::class.java)
                is ActionRequirement.Snapchat, is ActionRequirement.Shizuku, is ActionRequirement.Root -> false //Always needs to be checked later
                is ActionRequirement.Permission, is ActionRequirement.UserDisplayedActionRequirement -> throw RuntimeException("Not implemented")
            }
        } ?: true
    }

}