package com.jmvoty.votacion.features.polls.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "votes_to_sync")
data class VoteSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pollId: String,
    val optionId: String,
    val createdAt: Long = System.currentTimeMillis()
)