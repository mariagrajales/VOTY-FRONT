package com.example.votacion.features.polls.presentation.viewmodel

import com.example.votacion.features.polls.domain.entities.Poll

data class EditPollUiState(
    val poll: Poll? = null, // Usamos la Entidad, no el DTO
    val title: String = "",
    val options: List<String> = emptyList(),
    val isOpen: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val canEditOptions: Boolean = true
)