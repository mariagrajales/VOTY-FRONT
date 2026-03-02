package com.example.votacion.features.polls.domain.repository

import com.example.votacion.features.polls.data.models.PollOutput
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface PollRepository {
    fun getPolls(): Flow<List<PollOutput>>
    suspend fun refreshPolls()
    suspend fun getPoll(id: String): PollOutput
    suspend fun createPoll(title: String, options: List<String>): Response<PollOutput>
    suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>?): Response<PollOutput>
    suspend fun deletePoll(id: String)
    suspend fun castVote(pollId: String, optionId: String)
}
