package com.example.votacion.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, name: String, password: String)
    suspend fun logout()
    fun getTokenFlow(): Flow<String>
    suspend fun isAuthenticated(): Boolean
}