package com.example.votacion.features.auth.data.repository

import com.example.votacion.features.auth.data.models.AuthResponse
import com.example.votacion.features.auth.data.models.LoginRequest
import com.example.votacion.features.auth.data.models.RegisterRequest
import com.example.votacion.features.auth.data.network.AuthService
import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.core.data.TokenManager
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val authPreferences: AuthPreferences,
    private val tokenManager: TokenManager
) {
    suspend fun register(email: String, name: String, password: String): AuthResponse {
        val request = RegisterRequest(email, name, password)
        android.util.Log.d("AuthRepository", "Registering new user: $email")
        val response = authService.register(request)
        android.util.Log.d("AuthRepository", "Register response received with token: ${response.token.take(20)}... (Length: ${response.token.length})")
        
        // Save token using TokenManager (synchronous, encrypted)
        tokenManager.saveToken(response.token)
        
        // Save user data
        authPreferences.saveUser(response.user.id, response.user.email, response.user.name)
        android.util.Log.d("AuthRepository", "Token and user data saved")
        
        return response
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val request = LoginRequest(email, password)
        android.util.Log.d("AuthRepository", "Logging in with email: $email")
        val response = authService.login(request)
        android.util.Log.d("AuthRepository", "Login response received with token: ${response.token.take(20)}... (Length: ${response.token.length})")
        
        // Save token using TokenManager (synchronous, encrypted)
        tokenManager.saveToken(response.token)
        
        // Save user data
        authPreferences.saveUser(response.user.id, response.user.email, response.user.name)
        android.util.Log.d("AuthRepository", "Token and user data saved")
        
        return response
    }

    suspend fun logout() {
        tokenManager.clearToken()
        authPreferences.clear()
    }

    fun getTokenFlow() = authPreferences.getToken()
    fun getUserEmailFlow() = authPreferences.getUserEmail()
    fun getUserNameFlow() = authPreferences.getUserName()
    fun getUserIdFlow() = authPreferences.getUserId()
}
