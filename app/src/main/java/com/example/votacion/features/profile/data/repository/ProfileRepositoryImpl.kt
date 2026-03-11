package com.example.votacion.features.profile.data.repository

import com.example.votacion.features.profile.data.mapper.toDomain
import com.example.votacion.features.profile.data.models.UpdateProfileRequest
import com.example.votacion.features.profile.data.network.ProfileService
import com.example.votacion.features.profile.domain.entities.UserProfile
import com.example.votacion.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileService: ProfileService
) : ProfileRepository {

    override suspend fun getProfile(): UserProfile {
        val response = profileService.getProfile()
        return response.toDomain()
    }

    override suspend fun updateProfile(name: String?, avatar: String?): UserProfile {
        val request = UpdateProfileRequest(name = name, avatar = avatar)
        val response = profileService.updateProfile(request)
        return response.toDomain() // Convertimos a UserProfile
    }
}