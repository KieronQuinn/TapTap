package com.kieronquinn.app.taptap.repositories.gates

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapGate
import com.kieronquinn.app.taptap.components.columbus.gates.custom.*
import com.kieronquinn.app.taptap.models.gate.GateDataTypes
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.models.gate.GateSupportedRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory.*
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabase
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.service.accessibility.TapTapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.doesHavePermissions
import com.kieronquinn.app.taptap.utils.extensions.getApplicationLabel
import com.kieronquinn.app.taptap.utils.extensions.isServiceRunning
import com.kieronquinn.app.taptap.utils.flow.FlowQueue
import com.kieronquinn.app.taptap.utils.foldable.FoldableProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class GatesRepository {

    abstract val onChanged: Flow<Unit>

    abstract suspend fun populateDefaultGates(context: Context)

    abstract suspend fun getNextGateIndex(): Int
    abstract suspend fun createTapTapGate(gate: Gate, context: Context, serviceLifecycle: Lifecycle): TapTapGate?
    abstract suspend fun getSavedGates(): List<Gate>

    abstract suspend fun clearAll()
    abstract suspend fun addGate(gate: Gate): Long
    abstract suspend fun moveGate(fromIndex: Int, toIndex: Int)
    abstract suspend fun setGateEnabled(id: Int, enabled: Boolean)
    abstract suspend fun removeGate(id: Int)

    abstract fun getFormattedDescriptionForGate(
        context: Context,
        gateDirectory: TapTapGateDirectory,
        extraData: String?
    ): CharSequence

    abstract suspend fun isGateRequirementSatisfied(
        context: Context,
        gateDirectory: TapTapGateDirectory
    ): Boolean

    abstract fun isGateSupported(
        context: Context,
        gateDirectory: TapTapGateDirectory
    ): Boolean

    abstract fun getGateSupportedRequirement(
        context: Context,
        gateDirectory: TapTapGateDirectory
    ): GateSupportedRequirement?

    abstract fun isGateDataSatisfied(
        context: Context,
        data: GateDataTypes,
        extraData: String
    ): Boolean

}

class GatesRepositoryImpl(database: TapTapDatabase): GatesRepository() {

    override val onChanged = MutableSharedFlow<Unit>()
    private val gatesDao = database.gatesDao()

    private val savedGates = gatesDao.getAllAsFlow().flowOn(Dispatchers.IO)
        .stateIn(GlobalScope, SharingStarted.Lazily, null)

    override suspend fun populateDefaultGates(context: Context) {
        val defaultGates = arrayOf(Pair(POWER_STATE, true), Pair(KEYBOARD_VISIBILITY, false))
            .mapIndexed { index, tapTapGate ->
                Gate(
                    name = tapTapGate.first.name,
                    enabled = tapTapGate.second,
                    index = index,
                    extraData = ""
                )
            }
        actionsQueue.add(GatesAction.GenericAction {
            defaultGates.forEach {
                gatesDao.insert(it)
            }
        })
    }

    override suspend fun getSavedGates() = savedGates.filterNotNull().first()

    override suspend fun getNextGateIndex(): Int = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(GatesAction.GatesListRequiringAction { gates ->
                it.resume((gates.maxOfOrNull { it.index } ?: -1) + 1)
            })
        }
    }

    override suspend fun clearAll() {
        GlobalScope.launch {
            actionsQueue.add(GatesAction.GenericAction {
                gatesDao.deleteAll()
            })
        }
    }

    override suspend fun addGate(gate: Gate): Long = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(GatesAction.GenericAction {
                it.resume(gatesDao.insert(gate))
            })
        }
    }

    override suspend fun removeGate(id: Int) {
        actionsQueue.add(GatesAction.GatesListRequiringAction { gates ->
            val gate = gates.firstOrNull { it.gateId == id } ?: return@GatesListRequiringAction
            gatesDao.delete(gate)
        })
    }

    override suspend fun moveGate(fromIndex: Int, toIndex: Int) {
        actionsQueue.add(GatesAction.GatesListRequiringAction { gates ->
            if (fromIndex < toIndex) {
                for (i in fromIndex until toIndex) {
                    Collections.swap(gates, i, i + 1)
                }
            } else {
                for (i in fromIndex downTo toIndex + 1) {
                    Collections.swap(gates, i, i - 1)
                }
            }
            gates.forEachIndexed { index, gate ->
                gate.index = index
                gatesDao.update(gate)
            }
        })
    }

    override suspend fun setGateEnabled(id: Int, enabled: Boolean) {
        actionsQueue.add(GatesAction.GatesListRequiringAction { gates ->
            val gate = gates.firstOrNull { it.gateId == id } ?: return@GatesListRequiringAction
            gate.enabled = enabled
            gatesDao.update(gate)
        })
    }

    sealed class GatesAction {
        data class GatesListRequiringAction(val block: suspend (actions: List<Gate>) -> Unit): GatesAction()
        data class GenericAction(val block: suspend () -> Unit): GatesAction()
    }

    private val actionsQueue = FlowQueue<GatesAction>()

    private fun setupActionsQueue() = GlobalScope.launch {
        actionsQueue.asFlow().debounce(500L).collect {
            while (true) {
                val item = actionsQueue.asQueue().removeFirstOrNull() ?: break
                when(item){
                    is GatesAction.GatesListRequiringAction -> {
                        item.block.invoke(getSavedGates())
                    }
                    is GatesAction.GenericAction -> {
                        item.block()
                    }
                }
            }
            onChanged.emit(Unit)
        }
    }

    init {
        setupActionsQueue()
    }

    override fun isGateDataSatisfied(
        context: Context,
        data: GateDataTypes,
        extraData: String
    ): Boolean {
        return when (data) {
            //Those that require a picker are satisfied if the extra data is included
            GateDataTypes.PACKAGE_NAME -> extraData.isNotBlank()
        }
    }

    override fun getGateSupportedRequirement(context: Context, gateDirectory: TapTapGateDirectory): GateSupportedRequirement? {
        return when (gateDirectory.gateSupportedRequirement) {
            is GateSupportedRequirement.MinSdk -> {
                return if(Build.VERSION.SDK_INT < gateDirectory.gateSupportedRequirement.version){
                    gateDirectory.gateSupportedRequirement
                }else null
            }
            is GateSupportedRequirement.Foldable -> {
                return if(!FoldableProvider.isCompatible(context)){
                    gateDirectory.gateSupportedRequirement
                }else null
            }
            else -> null
        }
    }

    override fun isGateSupported(context: Context, gateDirectory: TapTapGateDirectory): Boolean {
        return getGateSupportedRequirement(context, gateDirectory) == null
    }

    override fun getFormattedDescriptionForGate(
        context: Context,
        gateDirectory: TapTapGateDirectory,
        extraData: String?
    ): CharSequence {
        if (extraData?.isNotBlank() != true || gateDirectory.formattableDescription == null) {
            return context.getText(gateDirectory.descriptionRes)
        }
        val formattedText = when (gateDirectory) {
            APP_SHOWING -> context.packageManager.getApplicationLabel(extraData)
                ?: context.getString(R.string.item_action_app_uninstalled, extraData)
            else -> null
        } ?: run {
            return context.getText(gateDirectory.descriptionRes)
        }
        return context.getString(gateDirectory.formattableDescription, formattedText)
    }

    override suspend fun createTapTapGate(
        gate: Gate,
        context: Context,
        serviceLifecycle: Lifecycle
    ): TapTapGate? {
        val gateName = gate.name
        val tapGate = TapTapGateDirectory.values().firstOrNull { it.name == gateName } ?: return null
        return when(tapGate){
            POWER_STATE -> PowerStateGate(serviceLifecycle, context)
            POWER_STATE_INVERSE -> PowerStateInverseGate(serviceLifecycle, context)
            LOCK_SCREEN -> LockScreenStateGate(serviceLifecycle, context)
            LOCK_SCREEN_INVERSE -> LockScreenStateInverseGate(serviceLifecycle, context)
            CHARGING_STATE -> ChargingStateGate(serviceLifecycle, context)
            USB_STATE -> UsbStateGate(serviceLifecycle, context)
            CAMERA_VISIBILITY -> CameraVisibilityGate(serviceLifecycle, context)
            TELEPHONY_ACTIVITY -> TelephonyActivityGate(serviceLifecycle, context)
            APP_SHOWING -> AppVisibilityGate(serviceLifecycle, context, gate.extraData)
            KEYBOARD_VISIBILITY -> KeyboardVisibilityGate(serviceLifecycle, context)
            ORIENTATION_LANDSCAPE -> OrientationGate(serviceLifecycle, context, Configuration.ORIENTATION_LANDSCAPE)
            ORIENTATION_PORTRAIT -> OrientationGate(serviceLifecycle, context, Configuration.ORIENTATION_PORTRAIT)
            TABLE -> TableDetectionGate(serviceLifecycle, context)
            POCKET -> PocketDetectionGate(serviceLifecycle, context)
            HEADSET -> HeadsetGate(serviceLifecycle, context)
            HEADSET_INVERSE -> HeadsetInverseGate(serviceLifecycle, context)
            MUSIC -> MusicGate(serviceLifecycle, context)
            MUSIC_INVERSE -> MusicInverseGate(serviceLifecycle, context)
            ALARM -> AlarmGate(serviceLifecycle, context)
            FOLDABLE_CLOSED -> FoldableClosedGate(serviceLifecycle, context)
            FOLDABLE_OPEN -> FoldableOpenGate(serviceLifecycle, context)
            LOW_BATTERY -> LowBatteryGate(serviceLifecycle, context)
            BATTERY_SAVER -> BatterySaverGate(serviceLifecycle, context)
        }
    }

    override suspend fun isGateRequirementSatisfied(
        context: Context,
        gateDirectory: TapTapGateDirectory
    ): Boolean {
        return gateDirectory.gateRequirement?.any {
            when(it){
                is GateRequirement.ReadPhoneStatePermission -> context.doesHavePermissions(Manifest.permission.READ_PHONE_STATE)
                is GateRequirement.Accessibility -> context.isServiceRunning(TapTapAccessibilityService::class.java)
                is GateRequirement.Shizuku, GateRequirement.Root -> true //Checked elsewhere
                is GateRequirement.Permission, is GateRequirement.UserDisplayedGateRequirement -> throw RuntimeException("Not implemented")
            }
        } ?: true
    }

}