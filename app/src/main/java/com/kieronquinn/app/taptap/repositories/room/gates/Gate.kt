package com.kieronquinn.app.taptap.repositories.room.gates

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.taptap.utils.extensions.randomId

@Entity
data class Gate(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "gate_id") var gateId: Int = randomId(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "enabled") var enabled: Boolean,
    @ColumnInfo(name = "index") var index: Int,
    @ColumnInfo(name = "extra_data") val extraData: String
)
