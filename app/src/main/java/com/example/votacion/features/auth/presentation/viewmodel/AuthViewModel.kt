package com.example.votacion.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.core.hardware.VibrationManager
import com.example.votacion.features.auth.domain.usecase.CheckAuthUseCase
import com.example.votacion.features.auth.domain.usecase.LoginUseCase
import com.example.votacion.features.auth.domain.usecase.LogoutUseCase
import com.example.votacion.features.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkAuthUseCase: CheckAuthUseCase,
    private val vibrationManager: VibrationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            checkAuthUseCase().collect { isAuthenticated ->
                _uiState.update { it.copy(isAuthenticated = isAuthenticated) }
            }
        }
    }

    fun updateEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun updatePassword(password: String) = _uiState.update { it.copy(password = password) }
    fun updateName(name: String) = _uiState.update { it.copy(name = name) }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            loginUseCase(email, password).fold(
                onSuccess = {
                    vibrationManager.vibrateSuccess()
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                },
                onFailure = { error ->
                    vibrationManager.vibrateError()
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun register() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            registerUseCase(state.email, state.name, state.password).fold(
                onSuccess = {
                    vibrationManager.vibrateSuccess()
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "¡Cuenta creada! Inicia sesión.",
                        password = ""
                    )}
                },
                onFailure = { error ->
                    vibrationManager.vibrateError()
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { AuthUiState() } // Reset completo del estado
        }
    }
}