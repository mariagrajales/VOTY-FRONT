package com.example.votacion.features.auth.data.network

import com.example.votacion.features.auth.data.models.*
import retrofit2.http.*

interface AuthService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("profile")
    suspend fun getProfile(): UserResponse
}
