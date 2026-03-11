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
import retrofit2.HttpException
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
            
            // Sincronización destructiva: eliminamos lo que no esté en la nube
            // para evitar que encuestas borradas por otros persistan localmente
            val remoteIds = polls.map { it.id }
            val localPolls = pollDao.getAllPollsSync()
            localPolls.forEach { local ->
                if (local.id !in remoteIds) {
                    pollDao.deletePollById(local.id)
                }
            }

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

    override suspend fun createPoll(title: String, options: List<String>) {
        // Solo llamamos al servicio. Si falla, lanzará una excepción que el UseCase atrapará.
        pollService.createPoll(CreatePollRequest(title, options))
        // Opcional: Podrías llamar a refreshPolls() aquí para actualizar la DB local
    }

    override suspend fun updatePoll(
        id: String,
        title: String,
        isOpen: Boolean,
        options: List<String> // Quitamos el '?' para que coincida con la Interface
    ) {
        pollService.updatePoll(id, UpdatePollRequest(title, isOpen, options))
    }

    override suspend fun deletePoll(id: String) {
        try {
            pollService.deletePoll(id)
            // Si el servidor confirma la eliminación, lo borramos de Room
            pollDao.deletePollById(id)
        } catch (e: HttpException) {
            // Caso especial: Si el servidor devuelve 404 o 500 diciendo que NO EXISTE,
            // significa que ya se borró en la nube. Lo borramos localmente para limpiar.
            if (e.code() == 404 || (e.code() == 500 && e.message()?.contains("not found") == true)) {
                pollDao.deletePollById(id)
            }
            throw e // Re-lanzamos para que el ViewModel sepa que hubo un error (o que ya no existe)
        }
    }

    override suspend fun castVote(pollId: String, optionId: String) {
        pollService.castVote(pollId, optionId)
    }
}
