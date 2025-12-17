package com.gemini.wol.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemini.wol.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE pcId = :pcId")
    fun getSchedulesForPc(pcId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>
    
    @Query("SELECT * FROM schedule WHERE enabled = 1")
    suspend fun getEnabledSchedules(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}
