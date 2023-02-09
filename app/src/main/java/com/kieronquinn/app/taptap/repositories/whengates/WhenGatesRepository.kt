package com.kieronquinn.app.taptap.repositories.whengates

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.gates.TapTapWhenGate
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.TapTapDatabase
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGate
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateDouble
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateTriple
import com.kieronquinn.app.taptap.utils.extensions.getApplicationLabel
import com.kieronquinn.app.taptap.utils.flow.FlowQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class WhenGatesRepository {

    abstract val onChanged: Flow<Unit>

    abstract suspend fun createTapTapWhenGate(whenGate: WhenGate, context: Context, serviceLifecycle: Lifecycle): TapTapWhenGate?

    abstract suspend fun getNextWhenGateIndex(actionId: Int): Int
    abstract suspend fun getSavedWhenGates(actionId: Int): List<WhenGate>

    abstract suspend fun clearAll()
    abstract suspend fun getWhenGates(): List<WhenGate>
    abstract suspend fun getWhenGatesAsFlow(actionId: Int): Flow<List<WhenGate>>
    abstract suspend fun addWhenGate(whenGate: WhenGate): Long
    abstract suspend fun removeWhenGate(actionId: Int, id: Int)
    abstract suspend fun removeAllWhenGates(actionId: Int)

    abstract fun getFormattedDescriptionForWhenGate(
        context: Context,
        gateDirectory: TapTapGateDirectory,
        extraData: String?
    ): CharSequence

}

abstract class WhenGatesRepositoryDouble<A: WhenGatesRepositoryBase.WhenGatesAction>(database: TapTapDatabase, gatesRepository: GatesRepository): WhenGatesRepositoryBase<WhenGateDouble, A>(database, gatesRepository)
abstract class WhenGatesRepositoryTriple<A: WhenGatesRepositoryBase.WhenGatesAction>(database: TapTapDatabase, gatesRepository: GatesRepository): WhenGatesRepositoryBase<WhenGateTriple, A>(database, gatesRepository)

abstract class WhenGatesRepositoryBase<T: WhenGate, A: WhenGatesRepositoryBase.WhenGatesAction>(database: TapTapDatabase, private val gatesRepository: GatesRepository): WhenGatesRepository() {

    protected val whenGatesDao = database.whenGatesDao()
    protected val actionId = MutableStateFlow<Int?>(null)
    abstract fun getWhenGatesForAction(actionId: Int): Flow<List<T>>
    abstract val savedWhenGates: StateFlow<Pair<Int, List<T>>?>
    interface WhenGatesAction
    abstract val actionsQueue: FlowQueue<A>
    override val onChanged = MutableSharedFlow<Unit>()

    override suspend fun createTapTapWhenGate(
        whenGate: WhenGate,
        context: Context,
        serviceLifecycle: Lifecycle
    ): TapTapWhenGate? {
        val rawGate = Gate(
            gateId = whenGate.whenGateId,
            name = whenGate.name,
            enabled = true,
            index = whenGate.index,
            extraData = whenGate.extraData
        )
        val tapTapGate = gatesRepository.createTapTapGate(rawGate, context, serviceLifecycle) ?: return null
        return TapTapWhenGate(tapTapGate, whenGate.invert)
    }

    override suspend fun getSavedWhenGates(actionId: Int): List<T> {
        savedWhenGates.value?.let {
            if(it.first == actionId) {
                return it.second
            }
        }
        this.actionId.emit(actionId)
        return savedWhenGates.first { it != null && it.first == actionId }!!.second
    }

    override fun getFormattedDescriptionForWhenGate(
        context: Context,
        gateDirectory: TapTapGateDirectory,
        extraData: String?
    ): CharSequence {
        if (extraData?.isNotBlank() != true || gateDirectory.formattableWhenDescription == null) {
            return context.getText(gateDirectory.whenDescriptionRes)
        }
        val formattedText = when (gateDirectory) {
            TapTapGateDirectory.APP_SHOWING -> context.packageManager.getApplicationLabel(extraData)
                ?: context.getString(R.string.item_action_app_uninstalled, extraData)
            else -> null
        } ?: run {
            return context.getText(gateDirectory.whenDescriptionRes)
        }
        return context.getString(gateDirectory.formattableWhenDescription, formattedText)
    }

}

class WhenGatesRepositoryDoubleImpl(database: TapTapDatabase, gatesRepository: GatesRepository): WhenGatesRepositoryDouble<WhenGatesRepositoryDoubleImpl.WhenGatesActionDouble>(database, gatesRepository) {

    override val actionsQueue = FlowQueue<WhenGatesActionDouble>()

    override fun getWhenGatesForAction(actionId: Int) = whenGatesDao.getWhenGatesForActionDouble(actionId)

    sealed class WhenGatesActionDouble: WhenGatesAction {
        data class WhenGatesListRequiringAction(val block: suspend (actions: List<WhenGateDouble>) -> Unit): WhenGatesActionDouble()
        data class GenericAction(val block: suspend () -> Unit): WhenGatesActionDouble()
    }

    private fun setupActionsQueue() = GlobalScope.launch {
        actionsQueue.asFlow().debounce(500L).collect {
            while (true) {
                val item = actionsQueue.asQueue().removeFirstOrNull() ?: break
                when(item){
                    is WhenGatesActionDouble.WhenGatesListRequiringAction -> {
                        item.block.invoke(getSavedWhenGates(actionId.filterNotNull().first()))
                    }
                    is WhenGatesActionDouble.GenericAction -> {
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

    override suspend fun getWhenGates(): List<WhenGate> = withContext(Dispatchers.IO) {
        return@withContext whenGatesDao.getAllDouble()
    }

    override val savedWhenGates = actionId.filterNotNull().flatMapLatest { actionId  ->
        getWhenGatesForAction(actionId).map {
            Pair(actionId, it)
        }
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)

    override suspend fun removeWhenGate(actionId: Int, id: Int) {
        actionsQueue.add(WhenGatesActionDouble.WhenGatesListRequiringAction { whenGates ->
            val whenGate = whenGates.filter { it.actionId == actionId }.find { it.whenGateId == id } ?: return@WhenGatesListRequiringAction
            whenGatesDao.delete(whenGate)
        })
    }

    override suspend fun getWhenGatesAsFlow(actionId: Int): Flow<List<WhenGate>> {
        this.actionId.emit(actionId)
        return whenGatesDao.getWhenGatesForActionDouble(actionId)
    }

    override suspend fun removeAllWhenGates(actionId: Int) {
        actionsQueue.add(WhenGatesActionDouble.GenericAction {
            whenGatesDao.deleteAllDouble(actionId)
        })
    }

    override suspend fun clearAll() {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionDouble.GenericAction {
                whenGatesDao.deleteAllDouble()
            })
        }
    }

    override suspend fun addWhenGate(whenGate: WhenGate): Long = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionDouble.GenericAction {
                it.resume(whenGatesDao.insert(whenGate as WhenGateDouble))
            })
        }
    }

    override suspend fun getNextWhenGateIndex(actionId: Int): Int = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionDouble.WhenGatesListRequiringAction { whenGates ->
                it.resume((whenGates.filter { it.actionId == actionId }.maxOfOrNull { it.index } ?: -1) + 1)
            })
        }
    }

}

class WhenGatesRepositoryTripleImpl(database: TapTapDatabase, gatesRepository: GatesRepository): WhenGatesRepositoryTriple<WhenGatesRepositoryTripleImpl.WhenGatesActionTriple>(database, gatesRepository) {

    override val actionsQueue = FlowQueue<WhenGatesActionTriple>()

    override fun getWhenGatesForAction(actionId: Int) = whenGatesDao.getWhenGatesForActionTriple(actionId)

    sealed class WhenGatesActionTriple: WhenGatesAction {
        data class WhenGatesListRequiringAction(val block: suspend (actions: List<WhenGateTriple>) -> Unit): WhenGatesActionTriple()
        data class GenericAction(val block: suspend () -> Unit): WhenGatesActionTriple()
    }

    private fun setupActionsQueue() = GlobalScope.launch {
        actionsQueue.asFlow().debounce(500L).collect {
            while (true) {
                val item = actionsQueue.asQueue().removeFirstOrNull() ?: break
                when(item){
                    is WhenGatesActionTriple.WhenGatesListRequiringAction -> {
                        item.block.invoke(getSavedWhenGates(actionId.filterNotNull().first()))
                    }
                    is WhenGatesActionTriple.GenericAction -> {
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

    override suspend fun getWhenGates(): List<WhenGate> = withContext(Dispatchers.IO) {
        return@withContext whenGatesDao.getAllTriple()
    }

    override val savedWhenGates = actionId.filterNotNull().flatMapLatest { actionId  ->
        getWhenGatesForAction(actionId).map {
            Pair(actionId, it)
        }
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)

    override suspend fun removeWhenGate(actionId: Int, id: Int) {
        actionsQueue.add(WhenGatesActionTriple.WhenGatesListRequiringAction { whenGates ->
            val whenGate = whenGates.filter { it.actionId == actionId }.find { it.whenGateId == id } ?: return@WhenGatesListRequiringAction
            whenGatesDao.delete(whenGate)
        })
    }

    override suspend fun getWhenGatesAsFlow(actionId: Int): Flow<List<WhenGate>> {
        this.actionId.emit(actionId)
        return whenGatesDao.getWhenGatesForActionTriple(actionId)
    }

    override suspend fun removeAllWhenGates(actionId: Int) {
        actionsQueue.add(WhenGatesActionTriple.GenericAction {
            whenGatesDao.deleteAllTriple(actionId)
        })
    }

    override suspend fun clearAll() {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionTriple.GenericAction {
                whenGatesDao.deleteAllTriple()
            })
        }
    }

    override suspend fun addWhenGate(whenGate: WhenGate): Long = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionTriple.GenericAction {
                it.resume(whenGatesDao.insert(whenGate as WhenGateTriple))
            })
        }
    }

    override suspend fun getNextWhenGateIndex(actionId: Int): Int = suspendCoroutine {
        GlobalScope.launch {
            actionsQueue.add(WhenGatesActionTriple.WhenGatesListRequiringAction { whenGates ->
                it.resume((whenGates.filter { it.actionId == actionId }.maxOfOrNull { it.index } ?: -1) + 1)
            })
        }
    }

}