package com.kieronquinn.app.taptap.repositories.room.actions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.taptap.utils.extensions.randomId

abstract class Action(
    open val id: Int,
    open val actionId: Int,
    open var name: String,
    open var index: Int,
    open var extraData: String
)

@Entity
data class DoubleTapAction(
    @PrimaryKey(autoGenerate = true) override var id: Int = 0,
    @ColumnInfo(name = "action_id") override var actionId: Int = randomId(),
    @ColumnInfo(name = "name") override var name: String,
    @ColumnInfo(name = "index") override var index: Int,
    @ColumnInfo(name = "extra_data") override var extraData: String
): Action(id, actionId, name, index, extraData)

@Entity
data class TripleTapAction(
    @PrimaryKey(autoGenerate = true) override var id: Int = 0,
    @ColumnInfo(name = "action_id") override var actionId: Int = randomId(),
    @ColumnInfo(name = "name") override var name: String,
    @ColumnInfo(name = "index") override var index: Int,
    @ColumnInfo(name = "extra_data") override var extraData: String
): Action(id, actionId, name, index, extraData)
