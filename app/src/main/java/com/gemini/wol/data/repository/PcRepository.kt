package com.gemini.wol.data.repository

import com.gemini.wol.data.local.dao.PcDao
import com.gemini.wol.data.local.entity.PcEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PcRepository @Inject constructor(
    private val pcDao: PcDao
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
        pcDao.deletePc(pc)
    }
    
    suspend fun updateLastSeen(id: String, timestamp: Long) {
        pcDao.updateLastSeen(id, timestamp)
    }
}
