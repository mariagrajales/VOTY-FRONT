package com.jmvoty.votacion.features.polls.data.repository

import android.content.Context
import android.net.Uri
import com.jmvoty.votacion.features.polls.data.local.dao.PollDao
import com.jmvoty.votacion.features.polls.data.local.entities.VoteSyncEntity
import com.jmvoty.votacion.features.polls.data.local.entities.toDomain
import com.jmvoty.votacion.features.polls.data.local.entities.toEntity
import com.jmvoty.votacion.features.polls.data.models.PollOutput
import com.jmvoty.votacion.features.polls.data.models.UpdatePollRequest
import com.jmvoty.votacion.features.polls.data.network.PollService
import com.jmvoty.votacion.features.polls.presentation.viewmodel.OptionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class PollRepository @Inject constructor(
    private val pollService: PollService,
    private val pollDao: PollDao,
    @ApplicationContext private val context: Context
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
            throw e
        }
    }

    suspend fun createPoll(title: String, options: List<OptionDraft>) {
        android.util.Log.d("PollRepository", "Creating poll: $title")

        val optionStrings = options.joinToString(",") { it.text }

        val imageParts = options.mapNotNull { draft ->
            draft.imageUri?.let { uri ->
                uriToFile(context, uri)?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("images", file.name, requestFile)
                }
            }
        }

        try {
            val response = if (imageParts.isEmpty()) {
                pollService.createPollSimple(title, optionStrings)
            } else {
                pollService.createPollWithImages(title, optionStrings, imageParts)
            }

            if (response.isSuccessful) {
                android.util.Log.i("PollRepository", "Poll created successfully!")
            } else {
                android.util.Log.e("PollRepository", "Error: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "Exception: ${e.message}")
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) { null }
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

    suspend fun savePendingVote(pollId: String, optionId: String) {
        pollDao.insertPendingVote(VoteSyncEntity(pollId = pollId, optionId = optionId))
    }

    suspend fun getPendingVotes(): List<VoteSyncEntity> {
        return pollDao.getPendingVotes()
    }

    suspend fun clearPendingVote(id: Int) {
        pollDao.deletePendingVote(id)
    }
}
