package com.kieronquinn.app.taptap.repositories.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.actions.ActionsRepository
import com.kieronquinn.app.taptap.repositories.gates.GatesRepository
import com.kieronquinn.app.taptap.repositories.room.actions.ActionsDao
import com.kieronquinn.app.taptap.repositories.room.actions.DoubleTapAction
import com.kieronquinn.app.taptap.repositories.room.actions.TripleTapAction
import com.kieronquinn.app.taptap.repositories.room.gates.Gate
import com.kieronquinn.app.taptap.repositories.room.gates.GatesDao
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateDouble
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGateTriple
import com.kieronquinn.app.taptap.repositories.room.whengates.WhenGatesDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Database(
    entities = [DoubleTapAction::class, TripleTapAction::class, Gate::class, WhenGateDouble::class, WhenGateTriple::class],
    version = 1
)
abstract class TapTapRoomDatabase : RoomDatabase() {

    abstract fun actionsDao(): ActionsDao
    abstract fun gatesDao(): GatesDao
    abstract fun whenGatesDao(): WhenGatesDao

}

interface TapTapDatabase {

    val restartBus: Flow<Unit>

    fun actionsDao(): ActionsDao
    fun gatesDao(): GatesDao
    fun whenGatesDao(): WhenGatesDao

}

class TapTapDatabaseImpl(
    context: Context,
    private val settings: TapTapSettings
) : TapTapDatabase, KoinComponent {

    private val actionsRepository by inject<ActionsRepository>()
    private val gatesRepository by inject<GatesRepository>()
    override val restartBus = MutableSharedFlow<Unit>()

    companion object {
        private const val DATABASE_NAME = "taptap"
    }

    private val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populateWithDefaults(context)
        }
    }

    private val database = Room.databaseBuilder(
        context, TapTapRoomDatabase::class.java, DATABASE_NAME
    ).addCallback(callback).build()

    override fun actionsDao() = database.actionsDao()
    override fun gatesDao() = database.gatesDao()
    override fun whenGatesDao() = database.whenGatesDao()

    private fun populateWithDefaults(context: Context) = GlobalScope.launch {
        if(settings.settingsUpgraded.get()) return@launch
        actionsRepository.populateDefaultActions(context)
        gatesRepository.populateDefaultGates(context)
        delay(500L)
        restartBus.emit(Unit)
    }

}