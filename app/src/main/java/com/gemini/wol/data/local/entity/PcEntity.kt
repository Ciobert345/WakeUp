package com.gemini.wol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pc")
data class PcEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mac: String,
    val broadcastIp: String?,
    val port: Int = 9,
    val lastSeenEpoch: Long? = null,
    val useRelay: Boolean = true
)
