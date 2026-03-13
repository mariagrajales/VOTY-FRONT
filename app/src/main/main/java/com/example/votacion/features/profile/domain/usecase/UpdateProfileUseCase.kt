package com.example.votacion.features.profile.domain.usecase

import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.features.profile.domain.entities.UserProfile
import com.example.votacion.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(name: String? = null, avatar: String? = null): Result<UserProfile> {
        return try {
            val userId = authPreferences.userIdFlow.first()
            if (userId.isEmpty()) {
                return Result.failure(Exception("No se encontró el ID del usuario"))
            }
            val userProfile = repository.updateProfile(userId, name, avatar)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}