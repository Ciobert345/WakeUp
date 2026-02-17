package com.ciobert.wol.data.repository

import com.ciobert.wol.data.local.dao.ScheduleDao
import com.ciobert.wol.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getSchedulesForPc(pcId: String): Flow<List<ScheduleEntity>> = scheduleDao.getSchedulesForPc(pcId)
    
    fun getAllSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()

    suspend fun insertSchedule(schedule: ScheduleEntity) {
        scheduleDao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: ScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }
}
