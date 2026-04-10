package com.jmvoty.votacion.features.polls.presentation.viewmodel

import com.jmvoty.votacion.features.polls.domain.entities.Poll

data class PollsUiState(
    val polls: List<Poll> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed class PollsEvent {
    data class ShowError(val message: String) : PollsEvent()
    object PollDeleted : PollsEvent()
    object VoteCast : PollsEvent()
}