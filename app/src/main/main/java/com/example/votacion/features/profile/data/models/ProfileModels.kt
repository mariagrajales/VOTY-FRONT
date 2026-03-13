package com.example.votacion.features.profile.data.models

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val id: String,
    val email: String,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("avatar_image")
    val avatarImage: String? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val avatar: String? = null // Base64
)