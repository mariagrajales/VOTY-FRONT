package com.example.votacion.features.profile.domain.repository

import com.example.votacion.features.profile.domain.entities.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(name: String? = null, avatar: String? = null): UserProfile
}