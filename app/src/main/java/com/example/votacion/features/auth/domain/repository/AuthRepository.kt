package com.example.votacion.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.votacion.features.auth.data.models.AuthResponse

interface AuthRepository {
    suspend fun register(email: String, name: String, password: String): AuthResponse
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun logout()
    
    fun getTokenFlow(): Flow<String>
    fun getUserEmailFlow(): Flow<String>
    fun getUserNameFlow(): Flow<String>
    fun getUserIdFlow(): Flow<String>
}
