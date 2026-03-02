package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.domain.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PollsUiState(
    val polls: List<PollOutput> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed class PollsEvent {
    data class ShowError(val message: String) : PollsEvent()
    object PollDeleted : PollsEvent()
    object VoteCast : PollsEvent()
}

@HiltViewModel
class PollsViewModel @Inject constructor(
    private val pollRepository: PollRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollsUiState())
    val uiState: StateFlow<PollsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PollsEvent>()
    val eventFlow: SharedFlow<PollsEvent> = _eventFlow.asSharedFlow()

    init {
        observePolls()
        refreshPolls()
    }

    private fun observePolls() {
        viewModelScope.launch {
            pollRepository.getPolls()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { polls ->
                    _uiState.update { it.copy(polls = polls, isLoading = false) }
                }
        }
    }

    fun refreshPolls() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                pollRepository.refreshPolls()
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isRefreshing = false, 
                        error = e.message ?: "Error al sincronizar datos"
                    ) 
                }
                _eventFlow.emit(PollsEvent.ShowError("No se pudo actualizar desde el servidor. Mostrando datos locales."))
            }
        }
    }

    fun castVote(pollId: String, optionId: String) {
        val previousPolls = _uiState.value.polls
        
        // --- ACTUALIZACIÓN OPTIMISTA ---
        _uiState.update { state ->
            state.copy(
                polls = state.polls.map { poll ->
                    if (poll.id == pollId) {
                        poll.copy(
                            voted = true,
                            selectedOptionId = optionId,
                            options = poll.options.map { option ->
                                if (option.id == optionId) {
                                    option.copy(votesCount = option.votesCount + 1)
                                } else {
                                    option
                                }
                            }
                        )
                    } else {
                        poll
                    }
                }
            )
        }

        viewModelScope.launch {
            try {
                pollRepository.castVote(pollId, optionId)
                _eventFlow.emit(PollsEvent.VoteCast)
            } catch (e: Exception) {
                // --- ROLLBACK (Reversión) ---
                _uiState.update { it.copy(polls = previousPolls) }
                _eventFlow.emit(PollsEvent.ShowError("Error al votar: ${e.message}. Se ha revertido el cambio."))
            }
        }
    }

    fun deletePoll(pollId: String) {
        val previousPolls = _uiState.value.polls
        
        // Actualización optimista
        _uiState.update { state ->
            state.copy(polls = state.polls.filter { it.id != pollId })
        }

        viewModelScope.launch {
            try {
                pollRepository.deletePoll(pollId)
                _eventFlow.emit(PollsEvent.PollDeleted)
            } catch (e: Exception) {
                // Rollback
                _uiState.update { it.copy(polls = previousPolls) }
                _eventFlow.emit(PollsEvent.ShowError("Error al eliminar: ${e.message}"))
            }
        }
    }
}
