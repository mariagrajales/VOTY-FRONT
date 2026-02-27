package com.example.votacion.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.votacion.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState = mutableStateOf(AuthUiState())
    val uiState: State<AuthUiState> = _uiState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                authRepository.getTokenFlow().collect { token ->
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = token.isNotEmpty()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error checking auth status", e)
                _uiState.value = _uiState.value.copy(isAuthenticated = false)
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                android.util.Log.d("AuthViewModel", "Attempting login for $email")
                authRepository.login(email, password)
                android.util.Log.d("AuthViewModel", "Login successful, token saved to EncryptedSharedPreferences")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null,
                    email = "",
                    password = ""
                )
                android.util.Log.d("AuthViewModel", "Auth state updated to authenticated")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                authRepository.register(email, name, password)

                // IMPORTANTE: No ponemos isAuthenticated = true aquí
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false, // El usuario aún debe loguearse
                    error = "¡Cuenta creada! Por favor, inicia sesión.",
                    email = email, // Opcional: dejamos el email para el login
                    password = "",
                    name = ""
                )
                android.util.Log.d("AuthViewModel", "Register successful, staying in Register for navigation")
            } catch (e: retrofit2.HttpException) {
                // Extraemos el JSON de error que vimos en el Logcat
                val errorBody = e.response()?.errorBody()?.string()
                val userMessage = when {
                    errorBody?.contains("email already registered") == true ->
                        "Este correo ya está registrado. Intenta iniciar sesión."
                    e.code() == 400 -> "Datos inválidos. Revisa el formato de tu correo o nombre."
                    else -> "Error del servidor (${e.code()}). Inténtalo más tarde."
                }

                android.util.Log.e("AuthViewModel", "Register failed: $errorBody")
                _uiState.value = _uiState.value.copy(isLoading = false, error = userMessage)

            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Unexpected register error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sin conexión al servidor"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(
                isAuthenticated = false,
                email = "",
                password = "",
                name = ""
            )
        }
    }
}
