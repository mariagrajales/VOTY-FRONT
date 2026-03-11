package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.domain.repository.PollRepository
import javax.inject.Inject

class CastVoteUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(pollId: String, optionId: String): Result<Unit> {
        return try {
            repository.castVote(pollId, optionId)
            Result.success(Unit)
        } catch (e: Exception) {
            // Aquí podrías personalizar el mensaje según el tipo de excepción (ej. 403 Forbidden)
            Result.failure(e)
        }
    }
}