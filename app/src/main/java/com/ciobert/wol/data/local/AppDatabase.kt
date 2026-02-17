package com.ciobert.wol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ciobert.wol.data.local.dao.PcDao
import com.ciobert.wol.data.local.dao.ScheduleDao
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.data.local.entity.ScheduleEntity

@Database(entities = [PcEntity::class, ScheduleEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pcDao(): PcDao
    abstract fun scheduleDao(): ScheduleDao
    
    companion object {
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create new table with new schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pc_new` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `mac` TEXT NOT NULL, 
                        `internalIp` TEXT, 
                        `internalPort` INTEGER NOT NULL DEFAULT 9, 
                        `externalIp` TEXT, 
                        `externalPort` INTEGER NOT NULL DEFAULT 9, 
                        `statusCheckPort` INTEGER DEFAULT 80, 
                        `lastSeenEpoch` INTEGER, 
                        `useRelay` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // 2. Copy data from old table to new table
                // Mapping: broadcastIp -> internalIp, port -> internalPort
                database.execSQL("""
                    INSERT INTO pc_new (id, name, mac, internalIp, internalPort, lastSeenEpoch, useRelay)
                    SELECT id, name, mac, broadcastIp, port, lastSeenEpoch, useRelay FROM pc
                """)

                // 3. Drop old table
                database.execSQL("DROP TABLE pc")

                // 4. Rename new table to old table name
                database.execSQL("ALTER TABLE pc_new RENAME TO pc")
            }
        }
        
        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }
    }
}
