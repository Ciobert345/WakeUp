package com.gemini.wol.data.repository

import com.gemini.wol.data.local.dao.PcDao
import com.gemini.wol.data.local.dao.ScheduleDao
import com.gemini.wol.data.local.entity.PcEntity
import com.gemini.wol.worker.ScheduleManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PcRepository @Inject constructor(
    private val pcDao: PcDao,
    private val scheduleDao: ScheduleDao,
    private val scheduleManager: ScheduleManager
) {
    val allPcs: Flow<List<PcEntity>> = pcDao.getAllPcs()

    suspend fun getPcById(id: String): PcEntity? = pcDao.getPcById(id)

    suspend fun insertPc(pc: PcEntity) {
        pcDao.insertPc(pc)
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
