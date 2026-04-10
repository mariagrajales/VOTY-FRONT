package com.jmvoty.votacion.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jmvoty.votacion.features.polls.data.local.dao.PollDao
import com.jmvoty.votacion.features.polls.data.local.entities.OptionEntity
import com.jmvoty.votacion.features.polls.data.local.entities.PollEntity
import com.jmvoty.votacion.features.polls.data.local.entities.VoteSyncEntity

@Database(
    entities = [PollEntity::class, OptionEntity::class, VoteSyncEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pollDao(): PollDao
}
