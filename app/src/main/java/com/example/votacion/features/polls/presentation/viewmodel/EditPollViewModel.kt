package com.example.votacion.features.polls.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.votacion.features.polls.data.models.PollOutput
import com.example.votacion.features.polls.data.repository.PollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// state for edit screen

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

    private val _uiState = mutableStateOf(EditPollUiState())
    val uiState: State<EditPollUiState> = _uiState

    private val pollId: String? = savedStateHandle.get<String>("pollId")

    init {
        pollId?.let { loadPoll(it) }
    }

    private fun loadPoll(id: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val poll = pollRepository.getPoll(id)
                _uiState.value = _uiState.value.copy(
                    poll = poll,
                    title = poll.title,
                    options = poll.options.map { it.text }.toMutableList(),
                    isOpen = poll.isOpen,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar la encuesta"
                )
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun updateOption(index: Int, text: String) {
        val opts = _uiState.value.options.toMutableList()
        opts[index] = text
        _uiState.value = _uiState.value.copy(options = opts)
    }

    fun addOption() {
        val opts = _uiState.value.options.toMutableList()
        opts.add("")
        _uiState.value = _uiState.value.copy(options = opts)
    }

    fun removeOption(index: Int) {
        val opts = _uiState.value.options.toMutableList()
        if (opts.size > 2) {
            opts.removeAt(index)
            _uiState.value = _uiState.value.copy(options = opts)
        }
    }

    fun toggleOpen() {
        _uiState.value = _uiState.value.copy(isOpen = !_uiState.value.isOpen)
    }

    fun saveChanges() {
        val id = _uiState.value.poll?.id ?: return
        viewModelScope.launch {
            try {
                android.util.Log.d("EditPollViewModel", "Saving changes for poll: $id")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                pollRepository.updatePoll(
                    id,
                    _uiState.value.title,
                    _uiState.value.isOpen,
                    _uiState.value.options
                )
                android.util.Log.d("EditPollViewModel", "Poll updated successfully")
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } catch (e: Exception) {
                android.util.Log.e("EditPollViewModel", "Error updating poll", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar la encuesta"
                )
            }
        }
    }

    fun deletePoll() {
        val id = _uiState.value.poll?.id ?: return
        viewModelScope.launch {
            try {
                android.util.Log.d("EditPollViewModel", "Deleting poll: $id")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                pollRepository.deletePoll(id)
                android.util.Log.d("EditPollViewModel", "Poll deleted successfully")
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } catch (e: Exception) {
                android.util.Log.e("EditPollViewModel", "Error deleting poll", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al eliminar la encuesta"
                )
            }
        }
    }
}
