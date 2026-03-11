package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.core.hardware.VibrationManager
import com.example.votacion.features.polls.domain.usecase.CastVoteUseCase
import com.example.votacion.features.polls.domain.usecase.DeletePollUseCase
import com.example.votacion.features.polls.domain.usecase.GetPollsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PollsViewModel @Inject constructor(
    private val getPollsUseCase: GetPollsUseCase,
    private val castVoteUseCase: CastVoteUseCase,
    private val deletePollUseCase: DeletePollUseCase,
    private val vibrationManager: VibrationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollsUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PollsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadPolls()
    }

    fun loadPolls() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Si este devuelve Flow, se usa así:
            getPollsUseCase().collect { result ->
                result.fold(
                    onSuccess = { polls ->
                        _uiState.update { it.copy(polls = polls, isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message, isLoading = false) }
                    }
                )
            }
        }
    }

    fun castVote(pollId: String, optionId: String) {
        val previousPolls = _uiState.value.polls

        // --- ACTUALIZACIÓN OPTIMISTA ---
        applyOptimisticVote(pollId, optionId)

        viewModelScope.launch {
            val result = castVoteUseCase(pollId, optionId)
            result.fold(
                onSuccess = {
                    vibrationManager.vibrateSuccess()
                    _eventFlow.emit(PollsEvent.VoteCast)
                },
                onFailure = { error ->
                    vibrationManager.vibrateError()
                    // ROLLBACK: Si falla, regresamos al estado anterior
                    _uiState.update { it.copy(polls = previousPolls) }
                    _eventFlow.emit(PollsEvent.ShowError(error.message ?: "Error al votar"))
                }
            )
        }
    }

    fun deletePoll(pollId: String) {
        val previousPolls = _uiState.value.polls

        // --- ACTUALIZACIÓN OPTIMISTA ---
        _uiState.update { state ->
            state.copy(polls = state.polls.filter { it.id != pollId })
        }

        viewModelScope.launch {
            val result = deletePollUseCase(pollId)
            result.fold(
                onSuccess = {
                    vibrationManager.vibrateSuccess()
                    _eventFlow.emit(PollsEvent.PollDeleted)
                },
                onFailure = { error ->
                    vibrationManager.vibrateError()
                    // ROLLBACK
                    _uiState.update { it.copy(polls = previousPolls) }
                    _eventFlow.emit(PollsEvent.ShowError(error.message ?: "No se pudo eliminar"))
                }
            )
        }
    }

    private fun applyOptimisticVote(pollId: String, optionId: String) {
        _uiState.update { state ->
            state.copy(
                polls = state.polls.map { poll ->
                    if (poll.id == pollId) {
                        poll.copy(
                            voted = true,
                            selectedOptionId = optionId,
                            totalVotes = poll.totalVotes + 1,
                            options = poll.options.map { opt ->
                                if (opt.id == optionId) opt.copy(votesCount = opt.votesCount + 1)
                                else opt
                            }
                        )
                    } else poll
                }
            )
        }
    }
}