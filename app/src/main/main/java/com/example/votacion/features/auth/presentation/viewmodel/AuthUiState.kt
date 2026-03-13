package com.example.votacion.features.auth.presentation.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val userName: String = ""
)

// Eventos para navegación o efectos visuales fuera del estado
sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
    object RegisterSuccess : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}