package com.jmvoty.votacion.features.polls.data.repository

import android.content.Context
import android.net.Uri
import com.jmvoty.votacion.features.polls.data.local.dao.PollDao
import com.jmvoty.votacion.features.polls.data.local.entities.VoteSyncEntity
import com.jmvoty.votacion.features.polls.data.local.entities.toDomain
import com.jmvoty.votacion.features.polls.data.local.entities.toEntity
import com.jmvoty.votacion.features.polls.data.models.PollOutput
import com.jmvoty.votacion.features.polls.data.models.UpdatePollRequest
import com.jmvoty.votacion.features.polls.data.network.PollEvent
import com.jmvoty.votacion.features.polls.data.network.PollService
import com.jmvoty.votacion.features.polls.data.network.PollSocketService
import com.jmvoty.votacion.features.polls.domain.repository.PollRepository
import com.jmvoty.votacion.features.polls.presentation.viewmodel.OptionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepositoryImpl @Inject constructor(
    private val pollService: PollService,
    private val pollDao: PollDao,
    private val pollSocketService: PollSocketService,
    @ApplicationContext private val context: Context
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

    override suspend fun createPoll(title: String, options: List<OptionDraft>) {
        android.util.Log.i("DEBUG_REPO", "Preparando encuesta: $title")

        // 1. Título y Opciones: ¡YA NO SON RequestBody!
        // Solo necesitamos los Strings puros para enviarlos por @Query
        val titleString = title
        val optionStrings = options.joinToString(",") { it.text } // Resulta en "Go,Python"

        // 2. Imágenes: Siguen siendo MultipartBody.Part (esto está perfecto)
        val imageParts = options.mapIndexed { index, draft ->
            draft.imageUri?.let { uri ->
                val file = uriToFile(this.context, uri) ?: return@let null
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

                // El nombre de la llave debe ser "images"
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }
        }.filterNotNull()

        android.util.Log.d("DEBUG_REPO", "Enviando Título: $titleString")
        android.util.Log.d("DEBUG_REPO", "Enviando Opciones: $optionStrings")
        android.util.Log.d("DEBUG_REPO", "Total imágenes: ${imageParts.size}")

        // 3. Llamada al servicio
        // Ahora pasamos Strings y la lista de Partes
        val response = pollService.createPoll(titleString, optionStrings, imageParts)

        if (response.isSuccessful) {
            android.util.Log.i("DEBUG_REPO", "¡Éxito total!")
        } else {
            android.util.Log.e("DEBUG_REPO", "Error: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }

    override suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>) {
        pollService.updatePoll(id, UpdatePollRequest(title, isOpen, options))
        refreshPolls()
    }

    override suspend fun deletePoll(id: String) {
        try {
            pollService.deletePoll(id)
            pollDao.deletePollById(id)
        } catch (e: HttpException) {
            if (e.code() == 404 || (e.code() == 500 && e.message()?.contains("not found") == true)) {
                pollDao.deletePollById(id)
            }
            throw e
        }
    }

    override suspend fun castVote(pollId: String, optionId: String) {
        pollService.castVote(pollId, optionId)
        // Opcional: podrías refrescar aquí también si quieres datos exactos del servidor tras votar
    }

    override suspend fun savePendingVote(pollId: String, optionId: String) {
        pollDao.insertPendingVote(VoteSyncEntity(pollId = pollId, optionId = optionId))
    }

    override suspend fun getPendingVotes(): List<VoteSyncEntity> {
        return pollDao.getPendingVotes()
    }

    override suspend fun clearPendingVote(id: Int) {
        pollDao.deletePendingVote(id)
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
