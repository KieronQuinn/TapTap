package com.kieronquinn.app.taptap.repositories.room.gates

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GatesDao {

    @Query("DELETE from `gate`")
    fun deleteAll()

    @Query("SELECT * from `gate` order by `index`")
    fun getAll(): List<Gate>

    @Query("SELECT * from `gate` order by `index`")
    fun getAllAsFlow(): Flow<List<Gate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(gate: Gate): Long

    @Delete
    fun delete(gate: Gate)

    @Update
    fun update(gate: Gate)

}