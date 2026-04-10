package com.jmvoty.votacion.features.polls.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jmvoty.votacion.features.polls.data.worker.CreatePollWorker
import com.jmvoty.votacion.features.polls.domain.usecase.CreatePollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val createPollUseCase: CreatePollUseCase, // Debes crearlo
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePollUiState())
    val uiState = _uiState.asStateFlow()

    var pendingImageIndex: Int = -1

    fun updateTitle(title: String) = _uiState.update { it.copy(title = title) }

    fun addOption() = _uiState.update {
        it.copy(options = it.options + OptionDraft()) // Crear objeto, no string
    }
    fun updateOptionText(index: Int, text: String) = _uiState.update { state ->
        val newOptions = state.options.toMutableList().apply {
            if (index in indices) {
                // Aquí es donde estaba el error: debes copiar el objeto y cambiar el texto
                this[index] = this[index].copy(text = text)
            }
        }
        state.copy(options = newOptions)
    }


    fun updateOptionImage(uri: android.net.Uri?) = _uiState.update { state ->
        if (pendingImageIndex == -1) return@update state
        val newOptions = state.options.toMutableList().apply {
            this[pendingImageIndex] = this[pendingImageIndex].copy(imageUri = uri)
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
        if (currentState.title.isBlank()) return

        // 1. Preparar datos para el Worker
        val data = Data.Builder()
            .putString("title", currentState.title)
            .putStringArray("options", currentState.options.map { it.text }.toTypedArray())
            .putStringArray("image_uris", currentState.options.mapNotNull { it.imageUri?.toString() }.toTypedArray())
            .build()

        // 2. Configurar restricciones (Solo con Internet)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 3. Crear la petición
        val uploadRequest = OneTimeWorkRequestBuilder<CreatePollWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        // 4. Encolar
        WorkManager.getInstance(context).enqueueUniqueWork(
            "create_poll_${System.currentTimeMillis()}",
            ExistingWorkPolicy.KEEP,
            uploadRequest
        )

        // Informar éxito inmediato (UI) indicando que se está subiendo en segundo plano
        _uiState.update { it.copy(success = true) }
    }
}

