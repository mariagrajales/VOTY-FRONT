package com.jmvoty.votacion.features.polls.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jmvoty.votacion.features.polls.data.local.entities.OptionEntity
import com.jmvoty.votacion.features.polls.data.local.entities.PollEntity
import com.jmvoty.votacion.features.polls.data.local.entities.VoteSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolls(polls: List<PollEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<OptionEntity>)

    @Transaction
    suspend fun insertPollsWithOptions(polls: List<PollEntity>, options: List<OptionEntity>) {
        insertPolls(polls)
        polls.forEach { deleteOptionsByPollId(it.id) }
        insertOptions(options)
    }

    @Query("DELETE FROM poll_options WHERE pollId = :pollId")
    suspend fun deleteOptionsByPollId(pollId: String)

    @Query("SELECT * FROM polls")
    suspend fun getAllPollsSync(): List<PollEntity>

    @Query("SELECT * FROM poll_options")
    suspend fun getAllOptionsSync(): List<OptionEntity>

    @Query("SELECT * FROM polls")
    fun getAllPolls(): Flow<List<PollEntity>>

    @Query("SELECT * FROM poll_options")
    fun getAllOptionsForFlow(): Flow<List<OptionEntity>>

    @Query("SELECT * FROM poll_options WHERE pollId = :pollId")
    fun getOptionsForPoll(pollId: String): Flow<List<OptionEntity>>

    @Query("DELETE FROM polls WHERE id = :pollId")
    suspend fun deletePollById(pollId: String)

    @Query("DELETE FROM polls")
    suspend fun deleteAllPolls()

    @Query("DELETE FROM poll_options")
    suspend fun deleteAllOptions()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingVote(vote: VoteSyncEntity)

    @Query("SELECT * FROM votes_to_sync ORDER BY createdAt ASC")
    suspend fun getPendingVotes(): List<VoteSyncEntity> // Aquí se resuelve 'getPendingVotes'

    @Query("DELETE FROM votes_to_sync WHERE id = :id")
    suspend fun deletePendingVote(id: Int) // Aquí se resuelve 'deletePendingVote'
}
