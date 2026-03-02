package com.example.votacion.features.auth.data.repository

import com.example.votacion.features.auth.data.models.AuthResponse
import com.example.votacion.features.auth.data.models.LoginRequest
import com.example.votacion.features.auth.data.models.RegisterRequest
import com.example.votacion.features.auth.data.network.AuthService
import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.core.data.TokenManager
import com.example.votacion.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val authPreferences: AuthPreferences,
    private val tokenManager: TokenManager
) : AuthRepository {
    
    override suspend fun register(email: String, name: String, password: String): AuthResponse {
        try {
            val request = RegisterRequest(
                email = email,
                name = name,
                password = password
            )

            android.util.Log.d("AuthRepository", "Registering new user: $email")

            val response = authService.register(request)

            android.util.Log.d("AuthRepository", "Register response received for: ${response.user.email}")

            tokenManager.saveToken(response.token)
            authPreferences.saveUser(
                userId = response.user.id,
                userEmail = response.user.email,
                userName = response.user.name
            )

            android.util.Log.d("AuthRepository", "Token and user data saved successfully")

            return response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "Registration error: $errorBody")
            throw e
        }
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        val request = LoginRequest(email, password)
        android.util.Log.d("AuthRepository", "Logging in with email: $email")
        val response = authService.login(request)
        
        tokenManager.saveToken(response.token)
        authPreferences.saveUser(response.user.id, response.user.email, response.user.name)
        
        return response
    }

    override suspend fun logout() {
        tokenManager.clearToken()
        authPreferences.clear()
    }

    override fun getTokenFlow(): Flow<String> = authPreferences.getToken()
    override fun getUserEmailFlow(): Flow<String> = authPreferences.getUserEmail()
    override fun getUserNameFlow(): Flow<String> = authPreferences.getUserName()
    override fun getUserIdFlow(): Flow<String> = authPreferences.getUserId()
}
