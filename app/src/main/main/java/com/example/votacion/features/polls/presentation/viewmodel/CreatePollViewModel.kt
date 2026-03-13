package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.polls.data.repository.PollRepository
import com.example.votacion.features.polls.domain.usecase.CreatePollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val createPollUseCase: CreatePollUseCase // Debes crearlo
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePollUiState())
    val uiState = _uiState.asStateFlow()

    fun updateTitle(title: String) = _uiState.update { it.copy(title = title) }

    fun addOption() = _uiState.update { it.copy(options = it.options + "") }

    fun updateOption(index: Int, text: String) = _uiState.update { state ->
        val newOptions = state.options.toMutableList().apply {
            if (index in indices) this[index] = text
        }
        state.copy(options = newOptions)
    }

    fun removeOption(index: Int) = _uiState.update { state ->
        if (state.options.size > 2) {
            state.copy(options = state.options.toMutableList().apply { removeAt(index) })
        } else state
    }

    fun createPoll() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "El título es requerido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = createPollUseCase(currentState.title, currentState.options.filter { it.isNotBlank() })

            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }
}