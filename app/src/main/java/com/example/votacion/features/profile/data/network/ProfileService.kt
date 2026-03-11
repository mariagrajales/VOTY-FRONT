package com.example.votacion.features.profile.data.network

import com.example.votacion.features.profile.data.models.ProfileResponse
import com.example.votacion.features.profile.data.models.UpdateProfileRequest
import retrofit2.http.*

interface ProfileService {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body request: UpdateProfileRequest
    ): ProfileResponse
}