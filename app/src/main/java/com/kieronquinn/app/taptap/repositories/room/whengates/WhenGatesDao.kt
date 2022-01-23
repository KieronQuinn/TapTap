package com.kieronquinn.app.taptap.repositories.room.whengates

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WhenGatesDao {

    @Query("DELETE from `whengatedouble`")
    fun deleteAllDouble()

    @Query("SELECT * from `whengatedouble` where action_id=:actionId order by `index`")
    fun getWhenGatesForActionDouble(actionId: Int): Flow<List<WhenGateDouble>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(whenGate: WhenGateDouble): Long

    @Query("SELECT * from `whengatedouble`")
    fun getAllDouble(): List<WhenGateDouble>

    @Delete
    fun delete(whenGate: WhenGateDouble)

    @Query("DELETE from `whengatedouble` where action_id=:actionId")
    fun deleteAllDouble(actionId: Int)

    @Query("DELETE from `whengatetriple`")
    fun deleteAllTriple()

    @Query("SELECT * from `whengatetriple` where action_id=:actionId order by `index`")
    fun getWhenGatesForActionTriple(actionId: Int): Flow<List<WhenGateTriple>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(whenGate: WhenGateTriple): Long

    @Query("SELECT * from `whengatetriple`")
    fun getAllTriple(): List<WhenGateTriple>

    @Delete
    fun delete(whenGate: WhenGateTriple)

    @Query("DELETE from `whengatetriple` where action_id=:actionId")
    fun deleteAllTriple(actionId: Int)

}