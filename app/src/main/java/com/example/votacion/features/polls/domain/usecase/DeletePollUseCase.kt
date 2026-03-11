package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.domain.repository.PollRepository
import retrofit2.HttpException
import javax.inject.Inject

class DeletePollUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(pollId: String): Result<Unit> {
        return try {
            repository.deletePoll(pollId)
            Result.success(Unit)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                403 -> "No tienes permiso para eliminar esta encuesta."
                404 -> "La encuesta ya no existe."
                else -> "Error del servidor al eliminar."
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}