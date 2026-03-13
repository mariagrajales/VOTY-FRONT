package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.polls.domain.usecase.DeletePollUseCase
import com.example.votacion.features.polls.domain.usecase.GetPollUseCase
import com.example.votacion.features.polls.domain.usecase.GetPollsUseCase
import com.example.votacion.features.polls.domain.usecase.UpdatePollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditPollViewModel @Inject constructor(
    private val getPollUseCase: GetPollUseCase,
    private val updatePollUseCase: UpdatePollUseCase,
    private val deletePollUseCase: DeletePollUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPollUiState())
    val uiState = _uiState.asStateFlow()

    private val pollId: String? = savedStateHandle.get<String>("pollId")

    init {
        pollId?.let { loadPoll(it) }
    }

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

    private fun loadPoll(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Al usar el GetPollUseCase (singular), esto devuelve Result<Poll>
            getPollUseCase(id).fold(
                onSuccess = { poll ->
                    _uiState.update { it.copy(
                        poll = poll,
                        title = poll.title,
                        options = poll.options.map { it.text },
                        isOpen = poll.isOpen,
                        canEditOptions = poll.totalVotes == 0,
                        isLoading = false
                    )}
                },
                onFailure = { e: Throwable ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }

    fun toggleOpen() = _uiState.update { it.copy(isOpen = !it.isOpen) }

    fun saveChanges() {
        val id = pollId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = updatePollUseCase(id, _uiState.value.title, _uiState.value.isOpen, _uiState.value.options)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun deletePoll() {
        val id = pollId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deletePollUseCase(id).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

}