package com.example.votacion.core.di

import android.content.Context
import androidx.room.Room
import com.example.votacion.core.database.AppDatabase
import com.example.votacion.features.polls.data.local.dao.PollDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voty_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePollDao(database: AppDatabase): PollDao {
        return database.pollDao()
    }
}
