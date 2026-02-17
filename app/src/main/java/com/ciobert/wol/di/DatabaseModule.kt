package com.ciobert.wol.di

import android.content.Context
import androidx.room.Room
import com.ciobert.wol.data.local.AppDatabase
import com.ciobert.wol.data.local.dao.PcDao
import com.ciobert.wol.data.local.dao.ScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "wol_database"
        )
        .addCallback(AppDatabase.CALLBACK)
        .addMigrations(AppDatabase.MIGRATION_2_3)
        .build()
    }

    @Provides
    fun providePcDao(database: AppDatabase): PcDao {
        return database.pcDao()
    }

    @Provides
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }
}
