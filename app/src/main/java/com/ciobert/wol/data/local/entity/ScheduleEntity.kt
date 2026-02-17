package com.ciobert.wol.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "schedule",
    foreignKeys = [
        ForeignKey(
            entity = PcEntity::class,
            parentColumns = ["id"],
            childColumns = ["pcId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pcId")]
)
data class ScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pcId: String,
    val daysBitmap: Int, // bitmask LUN=1, MAR=2, ... DOM=64
    val timeHour: Int,
    val timeMinute: Int,
    val enabled: Boolean = true
)
