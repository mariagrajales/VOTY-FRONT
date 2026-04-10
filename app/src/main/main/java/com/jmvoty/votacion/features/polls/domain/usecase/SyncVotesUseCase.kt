package com.jmvoty.votacion.features.polls.domain.usecase

import com.jmvoty.votacion.features.polls.data.repository.PollRepository
import javax.inject.Inject

class SyncVotesUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val pending = repository.getPendingVotes()
            pending.forEach { vote ->
                repository.castVote(vote.pollId, vote.optionId)
                repository.clearPendingVote(vote.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}