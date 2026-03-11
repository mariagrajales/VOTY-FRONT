package com.example.votacion.features.polls.domain.usecase

import com.example.votacion.features.polls.domain.repository.PollRepository
import javax.inject.Inject

class UpdatePollUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(
        id: String,
        title: String,
        isOpen: Boolean,
        options: List<String>
    ): Result<Unit> {
        return try {
            repository.updatePoll(id, title, isOpen, options)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}