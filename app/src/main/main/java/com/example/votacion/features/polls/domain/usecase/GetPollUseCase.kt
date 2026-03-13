package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.data.mapper.toDomain
import com.example.votacion.features.polls.domain.entities.Poll
import com.example.votacion.features.polls.domain.repository.PollRepository
import javax.inject.Inject


class GetPollUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(id: String): Result<Poll> {
        return try {
            // 1. Obtenemos el DTO (PollOutput) del repositorio
            val pollOutput = repository.getPoll(id)

            // 2. Mapeamos el resultado a nuestra entidad de dominio Poll
            val poll = pollOutput.toDomain()

            Result.success(poll)
        } catch (e: Exception) {
            // 3. Capturamos cualquier error (404, error de red, etc.)
            Result.failure(e)
        }
    }
}