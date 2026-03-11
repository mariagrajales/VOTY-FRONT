package com.example.votacion.features.profile.domain.usecase

import com.example.votacion.features.profile.domain.entities.UserProfile
import com.example.votacion.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(name: String? = null, avatar: String? = null): Result<UserProfile> {
        return try {
            val userProfile = repository.updateProfile(name, avatar)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}