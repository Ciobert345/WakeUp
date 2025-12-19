package com.gemini.wol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gemini.wol.data.local.dao.PcDao
import com.gemini.wol.data.local.dao.ScheduleDao
import com.gemini.wol.data.local.entity.PcEntity
import com.gemini.wol.data.local.entity.ScheduleEntity

@Database(entities = [PcEntity::class, ScheduleEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pcDao(): PcDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }
    }
}
