package com.kieronquinn.app.taptap.repositories.room.actions

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionsDao {

    @Query("DELETE from `DoubleTapAction`")
    fun deleteAllDoubleTap()

    @Query("SELECT * from `doubletapaction` order by `index`")
    fun getAllDoubleTap(): List<DoubleTapAction>

    @Query("SELECT * from `doubletapaction` order by `index`")
    fun getAllDoubleTapAsFlow(): Flow<List<DoubleTapAction>>

    @Query("DELETE from `TripleTapAction`")
    fun deleteAllTripleTap()

    @Query("SELECT * from `tripletapaction` order by `index`")
    fun getAllTripleTap(): List<TripleTapAction>

    @Query("SELECT * from `tripletapaction` order by `index`")
    fun getAllTripleTapAsFlow(): Flow<List<TripleTapAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(action: DoubleTapAction): Long

    @Delete
    fun delete(action: DoubleTapAction)

    @Update
    fun update(action: DoubleTapAction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(action: TripleTapAction): Long

    @Delete
    fun delete(action: TripleTapAction)

    @Update
    fun update(action: TripleTapAction)

}