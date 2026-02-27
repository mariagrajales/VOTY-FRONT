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
                android.util.Log.d("AuthViewModel", "Attempting register for $email")
                authRepository.register(email, name, password)
                android.util.Log.d("AuthViewModel", "Register successful, token saved to EncryptedSharedPreferences")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null,
                    email = "",
                    password = "",
                    name = ""
                )
                android.util.Log.d("AuthViewModel", "Auth state updated to authenticated")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Register failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al registrarse"
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
