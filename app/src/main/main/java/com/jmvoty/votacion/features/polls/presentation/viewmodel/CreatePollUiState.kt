package com.jmvoty.votacion.features.polls.presentation.viewmodel

data class CreatePollUiState(
    val title: String = "",
    val options: List<OptionDraft> = listOf(OptionDraft(), OptionDraft()), // Cambiamos String por Objeto
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

data class OptionDraft(
    val text: String = "",
    val imageUri: android.net.Uri? = null // Para la imagen de la galería
)