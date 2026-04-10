package com.jmvoty.votacion.features.auth.data.repository

import com.jmvoty.votacion.core.data.AuthPreferences
import com.jmvoty.votacion.core.network.api.DeviceTokenRequest
import com.jmvoty.votacion.features.auth.data.models.LoginRequest
import com.jmvoty.votacion.features.auth.data.models.RegisterRequest
import com.jmvoty.votacion.features.auth.data.network.AuthService
import com.jmvoty.votacion.features.auth.domain.repository.AuthRepository
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
        val response = authService.login(LoginRequest(email, password))

        // Guardar token y datos del usuario, incluyendo el ID
        authPreferences.saveToken(response.token)
        authPreferences.saveUser(
            userId = response.user.id,
            userEmail = response.user.email,
            userName = response.user.name
        )
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
        return authPreferences.tokenFlow.first().isNotEmpty()
    }

    override suspend fun updateDeviceToken(token: String) {try {
        authService.updateDeviceToken(DeviceTokenRequest(token))
    } catch (e: Exception) {

    }
    }
}
