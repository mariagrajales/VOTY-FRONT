package com.jmvoty.votacion.features.polls.domain.usecase

import com.jmvoty.votacion.features.polls.domain.repository.PollRepository
import javax.inject.Inject

class SavePendingVoteUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(pollId: String, optionId: String) {
        repository.savePendingVote(pollId, optionId)
    }
}