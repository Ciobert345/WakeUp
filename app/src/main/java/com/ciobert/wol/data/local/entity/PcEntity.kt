package com.ciobert.wol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pc")
data class PcEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mac: String,
    // Renamed/Mapped from broadcastIp in migration
    val internalIp: String?, 
    val internalPort: Int = 9,
    val externalIp: String? = null,
    val externalPort: Int = 9,
    val statusCheckPort: Int? = 80, // Default to 80 if not set, or user defined
    val lastSeenEpoch: Long? = null,
    val useRelay: Boolean = true
)
