package com.gemini.wol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gemini.wol.data.local.dao.PcDao
import com.gemini.wol.data.local.dao.ScheduleDao
import com.gemini.wol.data.local.entity.PcEntity
import com.gemini.wol.data.local.entity.ScheduleEntity

@Database(entities = [PcEntity::class, ScheduleEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pcDao(): PcDao
    abstract fun scheduleDao(): ScheduleDao
}
