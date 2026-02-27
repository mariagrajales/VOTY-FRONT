package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.votacion.features.polls.data.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState = mutableStateOf(CreatePollUiState())
    val uiState: State<CreatePollUiState> = _uiState

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateOption(index: Int, text: String) {
        val newOptions = _uiState.value.options.toMutableList()
        if (index < newOptions.size) {
            newOptions[index] = text
        }
        _uiState.value = _uiState.value.copy(options = newOptions)
    }

    fun addOption() {
        val newOptions = _uiState.value.options.toMutableList()
        newOptions.add("")
        _uiState.value = _uiState.value.copy(options = newOptions)
    }

    fun removeOption(index: Int) {
        if (_uiState.value.options.size > 2) {
            val newOptions = _uiState.value.options.toMutableList()
            newOptions.removeAt(index)
            _uiState.value = _uiState.value.copy(options = newOptions)
        }
    }

    fun createPoll() {
        viewModelScope.launch {
            try {
                if (_uiState.value.title.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "El título es requerido")
                    return@launch
                }

                val nonEmptyOptions = _uiState.value.options.filter { it.isNotBlank() }
                if (nonEmptyOptions.size < 2) {
                    _uiState.value = _uiState.value.copy(error = "Se requieren al menos 2 opciones")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                android.util.Log.d("CreatePollViewModel", "Creating poll with title: ${_uiState.value.title}")
                pollRepository.createPoll(_uiState.value.title, nonEmptyOptions)
                android.util.Log.d("CreatePollViewModel", "Poll created successfully")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("CreatePollViewModel", "Error creating poll", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al crear la encuesta"
                )
            }
        }
    }

    fun resetForm() {
        _uiState.value = CreatePollUiState()
    }
}
