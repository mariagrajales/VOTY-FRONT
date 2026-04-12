package com.jmvoty.votacion.features.auth.presentation.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val isCheckingAuth: Boolean = true, // Nuevo: Indica si está verificando el token al inicio
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val userName: String = ""
)

sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
    object RegisterSuccess : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}
