package com.kieronquinn.app.taptap.repositories.room.whengates

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.taptap.utils.extensions.randomId

abstract class WhenGate(
    open var id: Int,
    open var whenGateId: Int,
    open val actionId: Int,
    open val name: String,
    open val invert: Boolean,
    open val index: Int,
    open val extraData: String,
)

@Entity
data class WhenGateDouble(
    @PrimaryKey(autoGenerate = true) override var id: Int = 0,
    @ColumnInfo(name = "when_gate_id") override var whenGateId: Int = randomId(),
    @ColumnInfo(name = "action_id") override val actionId: Int,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "invert") override val invert: Boolean,
    @ColumnInfo(name = "index") override val index: Int,
    @ColumnInfo(name = "extra_data") override val extraData: String
): WhenGate(id, whenGateId, actionId, name, invert, index, extraData)

@Entity
data class WhenGateTriple(
    @PrimaryKey(autoGenerate = true) override var id: Int = 0,
    @ColumnInfo(name = "when_gate_id") override var whenGateId: Int = randomId(),
    @ColumnInfo(name = "action_id") override val actionId: Int,
    @ColumnInfo(name = "name") override val name: String,
    @ColumnInfo(name = "invert") override val invert: Boolean,
    @ColumnInfo(name = "index") override val index: Int,
    @ColumnInfo(name = "extra_data") override val extraData: String
): WhenGate(id, whenGateId, actionId, name, invert, index, extraData)
