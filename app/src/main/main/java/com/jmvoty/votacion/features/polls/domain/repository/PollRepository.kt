package com.jmvoty.votacion.features.polls.domain.repository

import com.jmvoty.votacion.features.polls.data.local.entities.VoteSyncEntity
import com.jmvoty.votacion.features.polls.data.models.PollOutput
import com.jmvoty.votacion.features.polls.presentation.viewmodel.OptionDraft
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    fun getPolls(): Flow<List<PollOutput>>
    suspend fun refreshPolls()
    suspend fun castVote(pollId: String, optionId: String)
    suspend fun deletePoll(pollId: String)
    // Agrega aquí los métodos para Create y Edit cuando los refactorices
    suspend fun createPoll(title: String, options: List<OptionDraft>)
    suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>)
    suspend fun getPoll(id: String): PollOutput
    suspend fun savePendingVote(pollId: String, optionId: String)
    suspend fun getPendingVotes(): List<VoteSyncEntity> // O un modelo de dominio
    suspend fun clearPendingVote(id: Int)
}