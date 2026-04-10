package com.jmvoty.votacion.features.polls.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jmvoty.votacion.core.hardware.VibrationManager
import com.jmvoty.votacion.features.polls.domain.usecase.CastVoteUseCase
import com.jmvoty.votacion.features.polls.domain.usecase.DeletePollUseCase
import com.jmvoty.votacion.features.polls.domain.usecase.GetPollsUseCase
import com.jmvoty.votacion.features.polls.domain.usecase.RefreshPollsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jmvoty.votacion.core.hardware.ShakeDetector
import com.jmvoty.votacion.features.polls.data.worker.SyncVotesWorker
import com.jmvoty.votacion.features.polls.domain.usecase.SavePendingVoteUseCase
import java.util.concurrent.TimeUnit // AÑADIDO

@HiltViewModel
class PollsViewModel @Inject constructor(
    private val getPollsUseCase: GetPollsUseCase,
    private val refreshPollsUseCase: RefreshPollsUseCase,
    private val castVoteUseCase: CastVoteUseCase,
    private val deletePollUseCase: DeletePollUseCase,
    private val vibrationManager: VibrationManager,
    val shakeDetector: ShakeDetector,
    private val savePendingVoteUseCase: SavePendingVoteUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollsUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PollsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadPolls(isRefresh = true)
        loadPolls(isRefresh = true)
        observeShakeEvents()
    }

    fun loadPolls(isRefresh: Boolean = false) {
        if (isRefresh) {
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                refreshPollsUseCase().fold(
                    onSuccess = {
                        _uiState.update { it.copy(isRefreshing = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message, isRefreshing = false) }
                    }
                )
            }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getPollsUseCase().collect { result ->
                    result.fold(
                        onSuccess = { polls ->
                            _uiState.update { it.copy(polls = polls, isLoading = false, isRefreshing = false) }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(error = error.message, isLoading = false, isRefreshing = false) }
                        }
                    )
                }
            }
        }
    }

    private fun observeShakeEvents() {
        viewModelScope.launch {
            shakeDetector.shakeEvents.collect {
                loadPolls(isRefresh = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stopListening()
    }

    fun castVote(pollId: String, optionId: String) {
        val previousPolls = _uiState.value.polls
        applyOptimisticVote(pollId, optionId)

        viewModelScope.launch {
            val result = castVoteUseCase(pollId, optionId)

            result.fold(
                onSuccess = {
                    vibrationManager.vibrateSuccess()
                    _eventFlow.emit(PollsEvent.VoteCast)
                },
                onFailure = { error ->
                    savePendingVoteUseCase(pollId, optionId)
                    scheduleSyncWork()
                    vibrationManager.vibrateError()
                    _eventFlow.emit(PollsEvent.ShowError("Voto guardado localmente. Se enviará al recuperar conexión."))
                }
            )
        }
    }

    fun deletePoll(pollId: String) {
        val previousPolls = _uiState.value.polls
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

    private fun scheduleSyncWork() {
        // CORREGIDO: Sintaxis de Constraints.Builder()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncVotesWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_votes_work",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }
}