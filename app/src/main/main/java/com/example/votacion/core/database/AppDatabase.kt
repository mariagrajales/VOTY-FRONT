package com.example.votacion.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.votacion.features.polls.data.local.dao.PollDao
import com.example.votacion.features.polls.data.local.entities.OptionEntity
import com.example.votacion.features.polls.data.local.entities.PollEntity

@Database(
    entities = [PollEntity::class, OptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pollDao(): PollDao
}
