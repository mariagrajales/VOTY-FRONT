package com.example.votacion.features.polls.data.repository

import com.example.votacion.features.polls.data.local.dao.PollDao
import com.example.votacion.features.polls.data.local.entities.toDomain
import com.example.votacion.features.polls.data.local.entities.toEntity
import com.example.votacion.features.polls.data.models.CreatePollRequest
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.models.UpdatePollRequest
import com.example.votacion.features.polls.data.network.PollService
import retrofit2.Response
import javax.inject.Inject

class PollRepository @Inject constructor(
    private val pollService: PollService,
    private val pollDao: PollDao
) {
    suspend fun getPolls(): List<PollOutput> {
        android.util.Log.d("PollRepository", "Fetching all polls")
        return try {
            val polls = pollService.listPolls()
            android.util.Log.d("PollRepository", "Fetched ${polls.size} polls from network, saving to database")
            
            val pollEntities = polls.map { it.toEntity() }
            val optionEntities = polls.flatMap { poll -> 
                poll.options.map { it.toEntity(poll.id) } 
            }
            pollDao.insertPollsWithOptions(pollEntities, optionEntities)
            
            polls
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Error fetching from network, trying local database: ${e.message}")
            val localPolls = pollDao.getAllPollsSync()
            val localOptions = pollDao.getAllOptionsSync()
            
            if (localPolls.isNotEmpty()) {
                android.util.Log.d("PollRepository", "Returning ${localPolls.size} polls from local database")
                localPolls.map { poll ->
                    val options = localOptions
                        .filter { it.pollId == poll.id }
                        .map { it.toDomain() }
                    poll.toDomain(options)
                }
            } else {
                android.util.Log.e("PollRepository", "No local data found either")
                throw e
            }
        }
    }

    suspend fun getPoll(id: String): PollOutput {
        android.util.Log.d("PollRepository", "Fetching poll: $id")
        return try {
            pollService.getPoll(id)
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Error fetching poll from network: ${e.message}")
            // Fallback for single poll could be added here if needed
            throw e
        }
    }

    suspend fun createPoll(title: String, options: List<String>): Response<PollOutput> {
        android.util.Log.d("PollRepository", "Creating poll: $title")
        return pollService.createPoll(CreatePollRequest(title, options))
    }

    suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>?): Response<PollOutput> {
        val response = pollService.updatePoll(id, UpdatePollRequest(title, isOpen, options))
        if (!response.isSuccessful) {
            throw Exception("Error del servidor: ${response.code()}")
        }
        return response
    }

    suspend fun deletePoll(id: String) {
        pollService.deletePoll(id)
    }

    suspend fun castVote(pollId: String, optionId: String) {
        pollService.castVote(pollId, optionId)
    }
}
