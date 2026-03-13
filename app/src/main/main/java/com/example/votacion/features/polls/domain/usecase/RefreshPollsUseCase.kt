package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.domain.repository.PollRepository
import javax.inject.Inject

class RefreshPollsUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.refreshPolls()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
