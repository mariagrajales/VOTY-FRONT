package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.domain.entities.Poll
import com.example.votacion.features.polls.domain.repository.PollRepository
import com.example.votacion.features.polls.data.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPollsUseCase @Inject constructor(
    private val repository: PollRepository
) {
    // Fíjate: No es suspend, devuelve un Flow
    operator fun invoke(): Flow<Result<List<Poll>>> {
        return repository.getPolls().map { list ->
            try {
                Result.success(list.map { it.toDomain() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}