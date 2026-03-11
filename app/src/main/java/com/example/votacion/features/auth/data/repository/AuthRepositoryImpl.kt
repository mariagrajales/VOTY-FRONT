package com.example.votacion.features.auth.data.repository

import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.features.auth.data.models.LoginRequest
import com.example.votacion.features.auth.data.models.RegisterRequest
import com.example.votacion.features.auth.data.network.AuthService
import com.example.votacion.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String) {
        // 1. Llamada a la API
        val response = authService.login(LoginRequest(email, password))

        // 2. Guardar el token localmente si la llamada fue exitosa
        // (Retrofit lanzará excepción si el código no es 2xx, manejado por el Use Case)
        authPreferences.saveToken(response.token)
        authPreferences.saveUserName(response.user.name)
    }

    override suspend fun register(email: String, name: String, password: String) {
        authService.register(RegisterRequest(email, name, password))
    }

    override suspend fun logout() {
        authPreferences.clearData()
    }

    override fun getTokenFlow(): Flow<String> {
        return authPreferences.tokenFlow
    }

    override suspend fun isAuthenticated(): Boolean {
        // Obtenemos el valor actual del flow de forma síncrona/suspendida
        return authPreferences.tokenFlow.first().isNotEmpty()
    }
}