package com.gemini.wol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pcId: String,
    val daysBitmap: Int, // bitmask LUN=1, MAR=2, ... DOM=64
    val timeHour: Int,
    val timeMinute: Int,
    val enabled: Boolean = true
)
