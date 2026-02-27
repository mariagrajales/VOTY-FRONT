package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.polls.data.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePollUiState(
    val title: String = "",
    val options: List<String> = listOf("", ""),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val pollRepository: PollRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePollUiState())
    val uiState: StateFlow<CreatePollUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateOption(index: Int, text: String) {
        _uiState.update { state ->
            val newOptions = state.options.toMutableList()
            if (index < newOptions.size) {
                newOptions[index] = text
            }
            state.copy(options = newOptions)
        }
    }

    fun addOption() {
        _uiState.update { state ->
            state.copy(options = state.options + "")
        }
    }

    fun removeOption(index: Int) {
        _uiState.update { state ->
            if (state.options.size > 2) {
                val newOptions = state.options.toMutableList()
                newOptions.removeAt(index)
                state.copy(options = newOptions)
            } else {
                state
            }
        }
    }

    fun createPoll() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.title.isBlank()) {
                _uiState.update { it.copy(error = "El título es requerido") }
                return@launch
            }

            val nonEmptyOptions = currentState.options.filter { it.isNotBlank() }
            if (nonEmptyOptions.size < 2) {
                _uiState.update { it.copy(error = "Se requieren al menos 2 opciones") }
                return@launch
            }

            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                pollRepository.createPoll(currentState.title, nonEmptyOptions)
                _uiState.update { it.copy(isLoading = false, success = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error al crear la encuesta"
                    ) 
                }
            }
        }
    }

    fun resetForm() {
        _uiState.value = CreatePollUiState()
    }
}
