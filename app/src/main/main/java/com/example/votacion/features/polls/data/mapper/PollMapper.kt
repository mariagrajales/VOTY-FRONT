package com.example.votacion.features.polls.data.mapper

import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.domain.entities.Poll
import com.example.votacion.features.polls.domain.entities.PollOption

fun PollOutput.toDomain(): Poll {
    return Poll(
        id = this.id,
        title = this.title,
        isOpen = this.isOpen,
        voted = this.voted,
        selectedOptionId = this.selectedOptionId,
        totalVotes = this.options.sumOf { it.votesCount ?: 0 },
        options = this.options.map {
            PollOption(
                id = it.id,
                text = it.text,
                votesCount = it.votesCount ?: 0
            )
        }
    )
}