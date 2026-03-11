package com.example.votacion.features.polls.domain.repository

import com.example.votacion.features.polls.data.models.PollOutput
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    fun getPolls(): Flow<List<PollOutput>>
    suspend fun refreshPolls()
    suspend fun castVote(pollId: String, optionId: String)
    suspend fun deletePoll(pollId: String)
    // Agrega aquí los métodos para Create y Edit cuando los refactorices
    suspend fun createPoll(title: String, options: List<String>)
    suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>)
    suspend fun getPoll(id: String): PollOutput
}