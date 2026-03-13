package com.example.votacion.features.polls.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.votacion.features.polls.data.models.OptionOutput
import com.example.votacion.features.polls.data.models.PollOutput

@Entity(tableName = "polls")
data class PollEntity(
    @PrimaryKey val id: String,
    val title: String,
    val voted: Boolean,
    val selectedOptionId: String?,
    val isOpen: Boolean
)

@Entity(tableName = "poll_options")
data class OptionEntity(
    @PrimaryKey val id: String,
    val pollId: String,
    val text: String,
    val votesCount: Int
)

fun PollOutput.toEntity(): PollEntity {
    return PollEntity(
        id = id,
        title = title,
        voted = voted,
        selectedOptionId = selectedOptionId,
        isOpen = isOpen
    )
}

fun OptionOutput.toEntity(pollId: String): OptionEntity {
    return OptionEntity(
        id = id,
        pollId = pollId,
        text = text,
        votesCount = votesCount
    )
}

fun PollEntity.toDomain(options: List<OptionOutput>): PollOutput {
    return PollOutput(
        id = id,
        title = title,
        options = options,
        voted = voted,
        selectedOptionId = selectedOptionId,
        isOpen = isOpen
    )
}

fun OptionEntity.toDomain(): OptionOutput {
    return OptionOutput(
        id = id,
        text = text,
        votesCount = votesCount
    )
}
