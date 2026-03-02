package com.example.votacion.features.auth.domain.usecase

import com.example.votacion.features.auth.data.models.AuthResponse
import com.example.votacion.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, name: String, password: String): AuthResponse {
        return repository.register(email, name, password)
    }
}
