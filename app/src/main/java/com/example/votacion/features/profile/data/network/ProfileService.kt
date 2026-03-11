package com.example.votacion.features.profile.data.network

import com.example.votacion.features.profile.data.models.ProfileResponse
import com.example.votacion.features.profile.data.models.UpdateProfileRequest
import retrofit2.http.*

interface ProfileService {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse
}