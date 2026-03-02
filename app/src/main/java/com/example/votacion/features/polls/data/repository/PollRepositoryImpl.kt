package com.example.votacion.features.polls.data.repository

import com.example.votacion.features.polls.data.local.dao.PollDao
import com.example.votacion.features.polls.data.local.entities.toDomain
import com.example.votacion.features.polls.data.local.entities.toEntity
import com.example.votacion.features.polls.data.models.CreatePollRequest
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.models.UpdatePollRequest
import com.example.votacion.features.polls.data.network.PollEvent
import com.example.votacion.features.polls.data.network.PollService
import com.example.votacion.features.polls.data.network.PollSocketService
import com.example.votacion.features.polls.domain.repository.PollRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepositoryImpl @Inject constructor(
    private val pollService: PollService,
    private val pollDao: PollDao,
    private val pollSocketService: PollSocketService
) : PollRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        observeSocketEvents()
    }

    private fun observeSocketEvents() {
        repositoryScope.launch {
            pollSocketService.pollEvents.collect { event ->
                when (event) {
                    is PollEvent.PollCreated -> savePollToDb(event.poll)
                    is PollEvent.VoteCast -> savePollToDb(event.poll)
                    is PollEvent.PollDeleted -> pollDao.deletePollById(event.pollId)
                }
            }
        }
    }

    private suspend fun savePollToDb(poll: PollOutput) {
        val pollEntity = poll.toEntity()
        val optionEntities = poll.options.map { it.toEntity(poll.id) }
        pollDao.insertPollsWithOptions(listOf(pollEntity), optionEntities)
    }

    override fun getPolls(): Flow<List<PollOutput>> {
        return combine(
            pollDao.getAllPolls(),
            pollDao.getAllOptionsForFlow()
        ) { polls, options ->
            polls.map { poll ->
                val pollOptions = options
                    .filter { it.pollId == poll.id }
                    .map { it.toDomain() }
                poll.toDomain(pollOptions)
            }
        }
    }

    override suspend fun refreshPolls() {
        try {
            val polls = pollService.listPolls()
            val pollEntities = polls.map { it.toEntity() }
            val optionEntities = polls.flatMap { poll -> 
                poll.options.map { it.toEntity(poll.id) } 
            }
            pollDao.insertPollsWithOptions(pollEntities, optionEntities)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getPoll(id: String): PollOutput {
        return pollService.getPoll(id)
    }

    override suspend fun createPoll(title: String, options: List<String>): Response<PollOutput> {
        return pollService.createPoll(CreatePollRequest(title, options))
    }

    override suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>?): Response<PollOutput> {
        return pollService.updatePoll(id, UpdatePollRequest(title, isOpen, options))
    }

    override suspend fun deletePoll(id: String) {
        pollService.deletePoll(id)
    }

    override suspend fun castVote(pollId: String, optionId: String) {
        pollService.castVote(pollId, optionId)
    }
}
