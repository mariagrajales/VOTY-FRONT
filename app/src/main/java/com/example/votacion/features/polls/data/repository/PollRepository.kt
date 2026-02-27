package com.example.votacion.features.polls.data.repository

import com.example.votacion.features.polls.data.models.CreatePollRequest
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.network.PollService
import javax.inject.Inject

class PollRepository @Inject constructor(
    private val pollService: PollService
) {
    suspend fun getPolls(): List<PollOutput> {
        android.util.Log.d("PollRepository", "Fetching all polls")
        val polls = pollService.listPolls()
        android.util.Log.d("PollRepository", "Fetched ${polls.size} polls")
        return polls
    }

    suspend fun getPoll(id: String): PollOutput {
        android.util.Log.d("PollRepository", "Fetching poll: $id")
        return pollService.getPoll(id)
    }

    suspend fun createPoll(title: String, options: List<String>): PollOutput? {
        android.util.Log.d("PollRepository", "Creating poll: $title with ${options.size} options")
        return pollService.createPoll(CreatePollRequest(title, options))
    }

    suspend fun updatePoll(id: String, title: String, isOpen: Boolean, options: List<String>? = null): PollOutput? {
        android.util.Log.d("PollRepository", "Updating poll: $id, title: $title, isOpen: $isOpen")
        val body = mutableMapOf<String, Any>(
            "title" to title,
            "is_open" to isOpen
        )
        if (options != null) {
            body["options"] = options
        }
        return pollService.updatePoll(id, body)
    }

    suspend fun deletePoll(id: String) {
        try {
            android.util.Log.d("PollRepository", ">>> Starting deletion of poll: '$id'")
            if (id.isBlank()) {
                android.util.Log.e("PollRepository", "ERROR: Poll ID is blank!")
                throw IllegalArgumentException("Poll ID cannot be blank")
            }
            android.util.Log.d("PollRepository", "Calling pollService.deletePoll with ID: '$id'")
            pollService.deletePoll(id)
            android.util.Log.d("PollRepository", "<<< Poll deleted successfully: '$id'")
        } catch (e: Exception) {
            android.util.Log.e("PollRepository", "<<< ERROR deleting poll '$id': ${e.message}", e)
            throw e
        }
    }

    suspend fun castVote(pollId: String, optionId: String) {
        android.util.Log.d("PollRepository", "Casting vote on poll: $pollId for option: $optionId")
        pollService.castVote(pollId, optionId)
        android.util.Log.d("PollRepository", "Vote cast successfully")
    }
}
