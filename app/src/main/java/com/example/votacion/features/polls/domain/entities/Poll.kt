package com.example.votacion.features.polls.domain.entities

data class Poll(
    val id: String,
    val title: String,
    val options: List<PollOption>,
    val isOpen: Boolean,
    val voted: Boolean,
    val selectedOptionId: String?,
    val totalVotes: Int
)

data class PollOption(
    val id: String,
    val text: String,
    val votesCount: Int
)