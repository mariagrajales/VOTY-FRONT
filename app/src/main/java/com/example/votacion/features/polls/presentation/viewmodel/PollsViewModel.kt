package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState = mutableStateOf(PollsUiState())
    val uiState: State<PollsUiState> = _uiState

    init {
        loadPolls()
    }

    fun loadPolls() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val polls = pollRepository.getPolls()
                _uiState.value = _uiState.value.copy(
                    polls = polls,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar las encuestas"
                )
            }
        }
    }

    fun castVote(pollId: String, optionId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PollsViewModel", "Casting vote for poll: $pollId, option: $optionId")
                pollRepository.castVote(pollId, optionId)
                android.util.Log.d("PollsViewModel", "Vote cast successfully")
                // Reload polls to get updated vote counts
                loadPolls()
            } catch (e: Exception) {
                android.util.Log.e("PollsViewModel", "Error casting vote", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al votar"
                )
            }
        }
    }

    fun deletePoll(pollId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PollsViewModel", ">>> START: Deleting poll: '$pollId'")
                if (pollId.isBlank()) {
                    android.util.Log.e("PollsViewModel", "ERROR: pollId is blank!")
                    _uiState.value = _uiState.value.copy(
                        error = "ID de encuesta inválido"
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                android.util.Log.d("PollsViewModel", "Calling repository.deletePoll...")
                pollRepository.deletePoll(pollId)
                android.util.Log.d("PollsViewModel", "✓ Poll deleted successfully, reloading list...")
                // Reload polls list after deletion
                loadPolls()
                android.util.Log.d("PollsViewModel", "<<< END: Deletion complete")
            } catch (e: Exception) {
                android.util.Log.e("PollsViewModel", "<<< ERROR: Failed to delete poll: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }

    fun refreshPolls() {
        loadPolls()
    }
}
