package com.example.votacion.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val userName: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                authRepository.getTokenFlow().collect { token ->
                    _uiState.update { it.copy(isAuthenticated = token.isNotEmpty()) }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error checking auth status", e)
                _uiState.update { it.copy(isAuthenticated = false) }
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                android.util.Log.d("AuthViewModel", "Attempting login for $email")
                authRepository.login(email, password)
                android.util.Log.d("AuthViewModel", "Login successful")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        error = null,
                        email = "",
                        password = ""
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login failed", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                authRepository.register(email, name, password)

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = "¡Cuenta creada! Por favor, inicia sesión.",
                        email = email,
                        password = "",
                        name = ""
                    )
                }
                android.util.Log.d("AuthViewModel", "Register successful")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val userMessage = when {
                    errorBody?.contains("email already registered") == true ->
                        "Este correo ya está registrado. Intenta iniciar sesión."
                    e.code() == 400 -> "Datos inválidos. Revisa el formato de tu correo o nombre."
                    else -> "Error del servidor (${e.code()}). Inténtalo más tarde."
                }
                android.util.Log.e("AuthViewModel", "Register failed: $errorBody")
                _uiState.update { it.copy(isLoading = false, error = userMessage) }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Unexpected register error", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Sin conexión al servidor"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { 
                it.copy(
                    isAuthenticated = false,
                    email = "",
                    password = "",
                    name = ""
                )
            }
        }
    }
}
