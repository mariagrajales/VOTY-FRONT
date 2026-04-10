package com.jmvoty.votacion.features.profile.domain.usecase

import com.jmvoty.votacion.features.profile.domain.entities.UserProfile
import com.jmvoty.votacion.features.profile.domain.repository.ProfileRepository
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