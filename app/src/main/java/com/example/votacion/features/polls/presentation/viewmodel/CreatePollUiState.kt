package com.example.votacion.features.polls.presentation.viewmodel

data class CreatePollUiState(
    val title: String = "",
    val options: List<String> = listOf("", ""),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)