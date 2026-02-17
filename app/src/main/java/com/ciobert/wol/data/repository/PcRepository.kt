package com.ciobert.wol.data.repository

import com.ciobert.wol.data.local.dao.PcDao
import com.ciobert.wol.data.local.dao.ScheduleDao
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.worker.ScheduleManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PcRepository @Inject constructor(
    private val pcDao: PcDao,
    private val scheduleDao: ScheduleDao,
    private val scheduleManager: ScheduleManager
) {
    val allPcs: Flow<List<PcEntity>> = pcDao.getAllPcs()
    val allSchedules: Flow<List<com.ciobert.wol.data.local.entity.ScheduleEntity>> = scheduleDao.getAllSchedules()

    suspend fun getPcById(id: String): PcEntity? = pcDao.getPcById(id)

    suspend fun insertPc(pc: PcEntity) {
        pcDao.insertPc(pc)
    }

    suspend fun insertPcs(pcs: List<PcEntity>) {
        pcDao.insertPcs(pcs)
    }

    suspend fun insertSchedules(schedules: List<com.ciobert.wol.data.local.entity.ScheduleEntity>) {
        schedules.forEach { scheduleDao.insertSchedule(it) }
    }

    suspend fun rescheduleAll() {
        val enabledSchedules = scheduleDao.getEnabledSchedules()
        enabledSchedules.forEach { schedule ->
            val pc = pcDao.getPcById(schedule.pcId)
            if (pc != null) {
                scheduleManager.scheduleNextWake(pc, schedule.id, schedule.timeHour, schedule.timeMinute, schedule.daysBitmap)
            }
        }
    }

    suspend fun updatePc(pc: PcEntity) {
        pcDao.updatePc(pc)
    }

    suspend fun deletePc(pc: PcEntity) {
        // 1. Get all schedules for this PC
        val schedules = scheduleDao.getSchedulesForPc(pc.id).first()
        
        // 2. Cancel all Android alarms
        scheduleManager.cancelAllSchedulesForPc(schedules)
        
        // 3. Delete the PC (Cascade will handle database schedules)
        pcDao.deletePc(pc)
    }
    
    suspend fun updateLastSeen(id: String, timestamp: Long) {
        pcDao.updateLastSeen(id, timestamp)
    }
}
