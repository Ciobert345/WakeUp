package com.gemini.wol.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemini.wol.data.local.entity.PcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PcDao {
    @Query("SELECT * FROM pc")
    fun getAllPcs(): Flow<List<PcEntity>>

    @Query("SELECT * FROM pc WHERE id = :id")
    suspend fun getPcById(id: String): PcEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPc(pc: PcEntity)

    @Update
    suspend fun updatePc(pc: PcEntity)

    @Delete
    suspend fun deletePc(pc: PcEntity)
    
    @Query("UPDATE pc SET lastSeenEpoch = :timestamp WHERE id = :id")
    suspend fun updateLastSeen(id: String, timestamp: Long)
}
