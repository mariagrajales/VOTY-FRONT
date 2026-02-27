package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
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

data class EditPollUiState(
    val poll: PollOutput? = null,
    val title: String = "",
    val options: List<String> = emptyList(),
    val isOpen: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class EditPollViewModel @Inject constructor(
    private val pollRepository: PollRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPollUiState())
    val uiState: StateFlow<EditPollUiState> = _uiState.asStateFlow()

    private val pollId: String? = savedStateHandle.get<String>("pollId")

    init {
        pollId?.let { loadPoll(it) }
    }

    private fun loadPoll(id: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val poll = pollRepository.getPoll(id)
                _uiState.update { 
                    it.copy(
                        poll = poll,
                        title = poll.title,
                        options = poll.options.map { it.text },
                        isOpen = poll.isOpen,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar la encuesta"
                    )
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateOption(index: Int, text: String) {
        _uiState.update { state ->
            val opts = state.options.toMutableList()
            if (index < opts.size) {
                opts[index] = text
            }
            state.copy(options = opts)
        }
    }

    fun addOption() {
        _uiState.update { it.copy(options = it.options + "") }
    }

    fun removeOption(index: Int) {
        _uiState.update { state ->
            if (state.options.size > 2) {
                val opts = state.options.toMutableList()
                opts.removeAt(index)
                state.copy(options = opts)
            } else {
                state
            }
        }
    }

    fun toggleOpen() {
        _uiState.update { it.copy(isOpen = !it.isOpen) }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        val id = currentState.poll?.id ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                pollRepository.updatePoll(
                    id,
                    currentState.title,
                    currentState.isOpen,
                    currentState.options
                )
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al actualizar la encuesta"
                    )
                }
            }
        }
    }

    fun deletePoll() {
        val id = _uiState.value.poll?.id ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                pollRepository.deletePoll(id)
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al eliminar la encuesta"
                    )
                }
            }
        }
    }
}
