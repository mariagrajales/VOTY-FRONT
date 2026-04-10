package com.jmvoty.votacion.features.profile.domain.repository

import com.jmvoty.votacion.features.profile.domain.entities.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(userId: String, name: String? = null, avatar: String? = null): UserProfile
}