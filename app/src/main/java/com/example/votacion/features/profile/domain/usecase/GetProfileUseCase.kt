package com.example.votacion.features.profile.domain.usecase

import com.example.votacion.features.profile.data.models.ProfileResponse
import com.example.votacion.features.profile.domain.entities.UserProfile
import com.example.votacion.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return try {
            val profile = repository.getProfile()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}