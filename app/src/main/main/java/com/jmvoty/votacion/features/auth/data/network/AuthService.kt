package com.jmvoty.votacion.features.auth.data.network

import com.jmvoty.votacion.core.network.api.DeviceTokenRequest
import com.jmvoty.votacion.features.auth.data.models.*
import retrofit2.http.*

interface AuthService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("profile")
    suspend fun getProfile(): UserResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserResponse

    @POST("device-token")
    suspend fun updateDeviceToken(@Body request: DeviceTokenRequest)
}
