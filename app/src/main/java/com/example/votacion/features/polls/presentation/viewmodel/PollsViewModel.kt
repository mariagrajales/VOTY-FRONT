package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PollsUiState(
    val polls: List<PollOutput> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PollsViewModel @Inject constructor(
    private val pollRepository: PollRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollsUiState())
    val uiState: StateFlow<PollsUiState> = _uiState.asStateFlow()

    init {
        loadPolls()
    }

    fun loadPolls() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val polls = pollRepository.getPolls()
                _uiState.update { it.copy(polls = polls, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error al cargar las encuestas"
                    ) 
                }
            }
        }
    }

    fun castVote(pollId: String, optionId: String) {
        viewModelScope.launch {
            try {
                pollRepository.castVote(pollId, optionId)
                loadPolls() // Refresh list after voting
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error al votar") }
            }
        }
    }

    fun deletePoll(pollId: String) {
        viewModelScope.launch {
            try {
                if (pollId.isBlank()) return@launch
                _uiState.update { it.copy(isLoading = true, error = null) }
                pollRepository.deletePoll(pollId)
                loadPolls()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar: ${e.message ?: "Error desconocido"}"
                    )
                }
            }
        }
    }

    fun refreshPolls() {
        loadPolls()
    }
}
